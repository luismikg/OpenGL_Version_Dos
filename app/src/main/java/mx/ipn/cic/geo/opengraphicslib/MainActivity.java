package mx.ipn.cic.geo.opengraphicslib;

import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class MainActivity extends AppCompatActivity {
    private GLSurfaceView surfaceViewPantalla;
    private Toroide toroide;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        this.surfaceViewPantalla = findViewById(R.id.surfaceViewPantalla);
        this.surfaceViewPantalla.setEGLContextClientVersion(2);
        this.surfaceViewPantalla.setRenderer(new GLSurfaceView.Renderer() {
            @Override
            public void onSurfaceCreated(GL10 gl, EGLConfig config) {
                surfaceViewPantalla.setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
                toroide = new Toroide(getApplicationContext());
            }

            @Override
            public void onSurfaceChanged(GL10 gl, int width, int height) {
                GLES20.glViewport(0,0, width, height);
            }

            @Override
            public void onDrawFrame(GL10 gl) {
                toroide.drawObject();
            }
        });
    }
}
