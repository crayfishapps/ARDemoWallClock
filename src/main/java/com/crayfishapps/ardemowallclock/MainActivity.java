package com.crayfishapps.ardemowallclock;

import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.view.Gravity;
import android.view.MotionEvent;
import android.widget.Toast;
import com.google.ar.core.Anchor;
import com.google.ar.core.HitResult;
import com.google.ar.core.Plane;
import com.google.ar.core.Plane.Type;
import com.google.ar.sceneform.AnchorNode;
import com.google.ar.sceneform.math.Quaternion;
import com.google.ar.sceneform.math.Vector3;
import com.google.ar.sceneform.rendering.Color;
import com.google.ar.sceneform.rendering.ModelRenderable;
import com.google.ar.sceneform.rendering.PlaneRenderer;
import com.google.ar.sceneform.ux.ArFragment;
import com.google.ar.sceneform.ux.TransformableNode;

import java.util.Calendar;

public class MainActivity extends AppCompatActivity {

    private Anchor anchor = null;

    private Handler handler = new Handler();

    private ArFragment arFragment;

    private ModelRenderable tickmarksRenderable;
    private ModelRenderable hoursRenderable;
    private ModelRenderable minutesRenderable;
    private ModelRenderable secondsRenderable;

    private TransformableNode nodeHours = null;
    private TransformableNode nodeMinutes = null;
    private TransformableNode nodeSeconds = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        arFragment = (ArFragment) getSupportFragmentManager().findFragmentById(R.id.ux_fragment);

        arFragment
                .getArSceneView()
                .getPlaneRenderer()
                .getMaterial()
                .thenAccept(material -> material.setFloat3(PlaneRenderer.MATERIAL_COLOR, new Color(0.0f, 0.0f, 1.0f, 1.0f)) );

        ModelRenderable.builder()
                .setSource(this, R.raw.clock_face_tickmarks)
                .build()
                .thenAccept(renderable -> tickmarksRenderable = renderable)
                .exceptionally(
                        throwable -> {
                            Toast toast =
                                    Toast.makeText(this, "Unable to load clock dial", Toast.LENGTH_LONG);
                            toast.setGravity(Gravity.CENTER, 0, 0);
                            toast.show();
                            return null;
                        });

        ModelRenderable.builder()
                .setSource(this, R.raw.clock_face_hand_hour)
                .build()
                .thenAccept(renderable -> hoursRenderable = renderable)
                .exceptionally(
                        throwable -> {
                            Toast toast =
                                    Toast.makeText(this, "Unable to load hour hand", Toast.LENGTH_LONG);
                            toast.setGravity(Gravity.CENTER, 0, 0);
                            toast.show();
                            return null;
                        });

        ModelRenderable.builder()
                .setSource(this, R.raw.clock_face_hand_minute)
                .build()
                .thenAccept(renderable -> minutesRenderable = renderable)
                .exceptionally(
                        throwable -> {
                            Toast toast =
                                    Toast.makeText(this, "Unable to load minute hand", Toast.LENGTH_LONG);
                            toast.setGravity(Gravity.CENTER, 0, 0);
                            toast.show();
                            return null;
                        });

        ModelRenderable.builder()
                .setSource(this, R.raw.clock_face_hand_second)
                .build()
                .thenAccept(renderable -> secondsRenderable = renderable)
                .exceptionally(
                        throwable -> {
                            Toast toast =
                                    Toast.makeText(this, "Unable to load second hand", Toast.LENGTH_LONG);
                            toast.setGravity(Gravity.CENTER, 0, 0);
                            toast.show();
                            return null;
                        });

        arFragment.setOnTapArPlaneListener(
                (HitResult hitResult, Plane plane, MotionEvent motionEvent) -> {
                    if (tickmarksRenderable == null) {
                        return;
                    }

                    if (plane.getType() != Type.VERTICAL) {
                        return;
                    }

                    if (anchor != null) {
                        return;
                    }

                    // Create the anchor
                    anchor = hitResult.createAnchor();
                    AnchorNode anchorNode = new AnchorNode(anchor);
                    anchorNode.setParent(arFragment.getArSceneView().getScene());

                    Vector3 anchorUp = anchorNode.getUp();

                    // Create the transformable clock and add it to the anchor
                    TransformableNode tickmarks = new TransformableNode(arFragment.getTransformationSystem());
                    tickmarks.setParent(anchorNode);
                    tickmarks.setLookDirection(Vector3.up(), anchorUp);
                    tickmarks.setRenderable(tickmarksRenderable);

                    nodeHours = new TransformableNode(arFragment.getTransformationSystem());
                    nodeHours.setParent(tickmarks);
                    nodeHours.setRenderable(hoursRenderable);

                    nodeMinutes = new TransformableNode(arFragment.getTransformationSystem());
                    nodeMinutes.setParent(tickmarks);
                    nodeMinutes.setRenderable(minutesRenderable);

                    nodeSeconds = new TransformableNode(arFragment.getTransformationSystem());
                    nodeSeconds.setParent(tickmarks);
                    nodeSeconds.setRenderable(secondsRenderable);

                    // Rotate the hands
                    handler.postDelayed(runnable, 10);

                    // disable plane rendering
                    arFragment.getArSceneView().getPlaneRenderer().setEnabled(false);
                });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        handler.removeCallbacks(runnable);
    }

    private Runnable runnable = new Runnable() {
        @Override
        public void run() {

            Calendar now = Calendar.getInstance();
            int hours = now.get(Calendar.HOUR_OF_DAY);
            int minutes = now.get(Calendar.MINUTE);
            int seconds = now.get(Calendar.SECOND);

            if (hours > 12) {
                hours -= 12;
            }

            float angleHours = hours * 30 + minutes / 2 - 90.0f;
            float angleMinutes = minutes * 6;
            float angleSeconds = seconds * 6 + 90.0f;

            Quaternion quaternionHours = new Quaternion(new Vector3(0.0f, -1.0f, 0.0f), angleHours);
            nodeHours.setLocalRotation(quaternionHours);

            Quaternion quaternionMinutes = new Quaternion(new Vector3(0.0f, -1.0f, 0.0f), angleMinutes);
            nodeMinutes.setLocalRotation(quaternionMinutes);

            Quaternion quaternionSeconds = new Quaternion(new Vector3(0.0f, -1.0f, 0.0f), angleSeconds);
            nodeSeconds.setLocalRotation(quaternionSeconds);

            handler.postDelayed(this, 1000);
        }
    };
}
