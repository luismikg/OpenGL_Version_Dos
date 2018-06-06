package mx.ipn.cic.geo.opengraphicslib;

import android.content.Context;
import android.opengl.GLES20;
import android.opengl.Matrix;
import android.util.Log;

import org.apache.commons.io.IOUtils;

import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Toroide {
    private List<String> verticesList;
    private List<String> facesList;
    private Context context;
    private FloatBuffer verticesBuffer;
    private ShortBuffer facesBuffer;
    private int program = -1;
    private int vertexShader = -1;
    private int fragmentShader = -1;

    public Toroide(Context context) {
        this.context = context;
        // Inicializar las listas que almacenarán los vértices y las caras.
        this.initList();
        // Leer el archivo que contiene la información del toroide.
        this.readFile();
        // Reservar el espacio para los buffer de vértices y caras.
        this.initBuffer();
        // Leer los vertices.
        this.parsePutVertices();
        // Leer las caras.
        this.parsePutFaces();
        // Cargar y compilar los shaders.
        this.loadShaders();
        this.createProgram();
    }

    private void initList() {
        this.verticesList = new ArrayList<>();
        this.facesList = new ArrayList<>();
    }

    private void initBuffer()
    {
        // Create buffer for vertices
        ByteBuffer buffer1 = ByteBuffer.allocateDirect(this.verticesList.size() * 3 * 4);
        buffer1.order(ByteOrder.nativeOrder());
        this.verticesBuffer = buffer1.asFloatBuffer();

        // Create buffer for faces
        ByteBuffer buffer2 = ByteBuffer.allocateDirect(this.facesList.size() * 3 * 2);
        buffer2.order(ByteOrder.nativeOrder());
        this.facesBuffer = buffer2.asShortBuffer();
    }

    private void readFile() {
        try {
            // Open the OBJ file with a Scanner
            Scanner scanner = new Scanner(context.getAssets().open("toroide.obj"));

            // Loop through all its lines
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                if (line.startsWith("v ")) {
                    // Add vertex line to list of vertices
                    verticesList.add(line);
                } else if (line.startsWith("f ")) {
                    // Add face line to faces list
                    facesList.add(line);
                }
            }
            // Close the scanner
            scanner.close();
        } catch (Exception e) {
            Log.e("Toroide.java", "Error: " + e.toString());
        }
    }

    private void parsePutVertices()
    {
        for(String vertex: this.verticesList) {
            String coords[] = vertex.split(" "); // Split by space
            float x = Float.parseFloat(coords[1]);
            float y = Float.parseFloat(coords[2]);
            float z = Float.parseFloat(coords[3]);
            this.verticesBuffer.put(x);
            this.verticesBuffer.put(y);
            this.verticesBuffer.put(z);
        }
        verticesBuffer.position(0);
    }

    private void parsePutFaces()
    {
        for(String face: this.facesList) {
            String vertexIndices[] = face.split(" ");
            short vertex1 = Short.parseShort(vertexIndices[1]);
            short vertex2 = Short.parseShort(vertexIndices[2]);
            short vertex3 = Short.parseShort(vertexIndices[3]);
            this.facesBuffer.put((short)(vertex1 - 1));
            this.facesBuffer.put((short)(vertex2 - 1));
            this.facesBuffer.put((short)(vertex3 - 1));
        }
        this.facesBuffer.position(0);
    }

    private void loadShaders()
    {
        try {
            // Convert vertex_shader.txt to a string
            InputStream vertexShaderStream = context.getResources().openRawResource(R.raw.vertex_shader);
            String vertexShaderCode = IOUtils.toString(vertexShaderStream, Charset.defaultCharset());
            vertexShaderStream.close();

            // Convert fragment_shader.txt to a string
            InputStream fragmentShaderStream = context.getResources().openRawResource(R.raw.fragment_shader);
            String fragmentShaderCode = IOUtils.toString(fragmentShaderStream, Charset.defaultCharset());
            fragmentShaderStream.close();

            this.vertexShader = GLES20.glCreateShader(GLES20.GL_VERTEX_SHADER);
            GLES20.glShaderSource(this.vertexShader, vertexShaderCode);

            this.fragmentShader = GLES20.glCreateShader(GLES20.GL_FRAGMENT_SHADER);
            GLES20.glShaderSource(this.fragmentShader, fragmentShaderCode);

            GLES20.glCompileShader(this.vertexShader);
            GLES20.glCompileShader(this.fragmentShader);
        }
        catch(Exception e)
        {
            Log.e("Toroide.java", "Error: " + e.toString());
        }
    }

    private void createProgram()
    {
        this.program = GLES20.glCreateProgram();
        GLES20.glAttachShader(this.program, this.vertexShader);
        GLES20.glAttachShader(this.program, this.fragmentShader);

        GLES20.glLinkProgram(this.program);
        GLES20.glUseProgram(this.program);
    }

    public void drawObject()
    {
        int position = GLES20.glGetAttribLocation(this.program, "position");
        GLES20.glEnableVertexAttribArray(position);

        GLES20.glVertexAttribPointer(position,3, GLES20.GL_FLOAT, false, 3 * 4, verticesBuffer);

        float[] projectionMatrix = new float[16];
        float[] viewMatrix = new float[16];
        float[] productMatrix = new float[16];

        Matrix.frustumM(projectionMatrix, 0, -1, 1, -1, 1, 2, 9);
        Matrix.setLookAtM(viewMatrix, 0, 0, 3, -4, 0,
                0, 0, 0, 1, 0);
        Matrix.multiplyMM(productMatrix, 0, projectionMatrix, 0, viewMatrix, 0);

        int matrix = GLES20.glGetUniformLocation(program, "matrix");
        GLES20.glUniformMatrix4fv(matrix, 1, false, productMatrix, 0);

        GLES20.glDrawElements(GLES20.GL_TRIANGLES, facesList.size() * 3, GLES20.GL_UNSIGNED_SHORT, facesBuffer);
        GLES20.glDisableVertexAttribArray(position);
    }
}
