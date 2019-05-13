/*
 * Copyright 2018 Google LLC. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.amodtech.ar.lineview;

import android.app.Activity;
import android.support.design.widget.FloatingActionButton;
import android.app.ActivityManager;
import android.content.Context;
import android.os.Build;
import android.os.Build.VERSION_CODES;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;
import com.google.ar.core.Anchor;
import com.google.ar.core.Frame;
import com.google.ar.core.HitResult;
import com.google.ar.core.Plane;
import com.google.ar.core.Pose;
import com.google.ar.core.Session;
import com.google.ar.sceneform.AnchorNode;
import com.google.ar.sceneform.HitTestResult;
import com.google.ar.sceneform.Node;
import com.google.ar.sceneform.math.Quaternion;
import com.google.ar.sceneform.math.Vector3;
import com.google.ar.sceneform.rendering.Color;
import com.google.ar.sceneform.rendering.MaterialFactory;
import com.google.ar.sceneform.rendering.ModelRenderable;
//import com.google.ar.sceneform.samples.hellosceneform.R;
import com.google.ar.sceneform.rendering.ShapeFactory;
import com.google.ar.sceneform.ux.ArFragment;

import java.util.ArrayList;
import java.util.List;

/**
 * LineViewMainActivity - built on HelloSceneForm sample.
 */
public class LineViewMainActivity extends AppCompatActivity {
  private static final String TAG = LineViewMainActivity.class.getSimpleName();
  private static final double MIN_OPENGL_VERSION = 3.0;
  private  static final int MAX_ANCHORS = 2;

  private ArFragment arFragment;
  private ModelRenderable andyRenderable;
  private ModelRenderable foxRenderable;
  private AnchorNode anchorNode;
  private List<AnchorNode> anchorNodeList = new ArrayList<>();
  private Integer numberOfAnchors = 0;
  private AnchorNode currentSelectedAnchorNode = null;
  private Node nodeForLine;


    @Override
  @SuppressWarnings({"AndroidApiChecker", "FutureReturnValueIgnored"})
  // CompletableFuture requires api level 24
  // FutureReturnValueIgnored is not valid
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    if (!checkIsSupportedDeviceOrFinish(this)) {
      return;
    }

    setContentView(R.layout.activity_ux);
    arFragment = (ArFragment) getSupportFragmentManager().findFragmentById(R.id.ux_fragment);

    // When you build a Renderable, Sceneform loads its resources in the background while returning
    // a CompletableFuture. Call thenAccept(), handle(), or check isDone() before calling get().
    ModelRenderable.builder()
        .setSource(this, R.raw.andy)
        .build()
        .thenAccept(renderable -> andyRenderable = renderable)
        .exceptionally(
            throwable -> {
              Toast toast =
                  Toast.makeText(this, "Unable to load andy renderable", Toast.LENGTH_LONG);
              toast.setGravity(Gravity.CENTER, 0, 0);
              toast.show();
              return null;
            });

      ModelRenderable.builder()
              .setSource(this, R.raw.arcticfox_posed)
              .build()
              .thenAccept(renderable -> foxRenderable = renderable)
              .exceptionally(
                      throwable -> {
                          Toast toast =
                                  Toast.makeText(this, "Unable to load fox renderable", Toast.LENGTH_LONG);
                          toast.setGravity(Gravity.CENTER, 0, 0);
                          toast.show();
                          return null;
                      });

    arFragment.setOnTapArPlaneListener(
        (HitResult hitResult, Plane plane, MotionEvent motionEvent) -> {
          // Do nothng on plane taps for now

        });

      arFragment.getArSceneView().getScene().setOnPeekTouchListener(this::handleOnTouch);

        //Add a listener for the back button
        FloatingActionButton backButtom = findViewById(R.id.back_buttom);
        backButtom.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Move the anchor back
                Log.d(TAG,"Moving anchor back");
                if (currentSelectedAnchorNode != null) {
                    //Get the current Pose and transform it then set a new anchor at the new pose
                    Session session = arFragment.getArSceneView().getSession();
                    Anchor currentAnchor = currentSelectedAnchorNode.getAnchor();
                    Pose oldPose = currentAnchor.getPose();
                    Pose newPose = oldPose.compose(Pose.makeTranslation(0,0,-0.05f));
                    currentSelectedAnchorNode = moveRenderable(currentSelectedAnchorNode, newPose);
                }
            }
        });

        //Add a listener for the forward button
        FloatingActionButton forwardButtom = findViewById(R.id.forward_buttom);
        forwardButtom.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Move the anchor forward
                Log.d(TAG,"Moving anchor forward");
                if (currentSelectedAnchorNode != null) {
                    //Get the current Pose and transform it then set a new anchor at the new pose
                    Session session = arFragment.getArSceneView().getSession();
                    Anchor currentAnchor = currentSelectedAnchorNode.getAnchor();
                    Pose oldPose = currentAnchor.getPose();
                    Pose newPose = oldPose.compose(Pose.makeTranslation(0,0,0.05f));
                    currentSelectedAnchorNode = moveRenderable(currentSelectedAnchorNode, newPose);
                }
            }
        });

        //Add a listener for the left button
        FloatingActionButton leftButtom = findViewById(R.id.left_button);
        leftButtom.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Move the anchor left
                Log.d(TAG,"Moving anchor left");
                if (currentSelectedAnchorNode != null) {
                    //Get the current Pose and transform it then set a new anchor at the new pose
                    Session session = arFragment.getArSceneView().getSession();
                    Anchor currentAnchor = currentSelectedAnchorNode.getAnchor();
                    Pose oldPose = currentAnchor.getPose();
                    Pose newPose = oldPose.compose(Pose.makeTranslation(-0.05f,0,0));
                    currentSelectedAnchorNode = moveRenderable(currentSelectedAnchorNode, newPose);
                }
            }
        });

        //Add a listener for the right button
        FloatingActionButton rightButtom = findViewById(R.id.right_button);
        rightButtom.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Move the anchor right
                Log.d(TAG,"Moving anchor Right");
                if (currentSelectedAnchorNode != null) {
                    //Get the current Pose and transform it then set a new anchor at the new pose
                    Session session = arFragment.getArSceneView().getSession();
                    Anchor currentAnchor = currentSelectedAnchorNode.getAnchor();
                    Pose oldPose = currentAnchor.getPose();
                    Pose newPose = oldPose.compose(Pose.makeTranslation(0.05f,0,0));
                    currentSelectedAnchorNode = moveRenderable(currentSelectedAnchorNode, newPose);
                }
            }
        });

        //Add a listener for the up button
        FloatingActionButton upButtom = findViewById(R.id.up_button);
        upButtom.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Move the anchor up
                Log.d(TAG,"Moving anchor Up");
                if (currentSelectedAnchorNode != null) {
                    //Get the current Pose and transform it then set a new anchor at the new pose
                    Session session = arFragment.getArSceneView().getSession();
                    Anchor currentAnchor = currentSelectedAnchorNode.getAnchor();
                    Pose oldPose = currentAnchor.getPose();
                    Pose newPose = oldPose.compose(Pose.makeTranslation(0,0.05f,0));
                    currentSelectedAnchorNode = moveRenderable(currentSelectedAnchorNode, newPose);
                }
            }
        });

        //Add a listener for the down button
        FloatingActionButton downButtom = findViewById(R.id.down_button);
        downButtom.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Move the anchor down
                Log.d(TAG,"Moving anchor Down");
                if (currentSelectedAnchorNode != null) {
                    //Get the current Pose and transform it then set a new anchor at the new pose
                    Session session = arFragment.getArSceneView().getSession();
                    Anchor currentAnchor = currentSelectedAnchorNode.getAnchor();
                    Pose oldPose = currentAnchor.getPose();
                    Pose newPose = oldPose.compose(Pose.makeTranslation(0,-0.05f,0));
                    currentSelectedAnchorNode = moveRenderable(currentSelectedAnchorNode, newPose);
                }
            }
        });

        //Add a listener for the drawline button
        FloatingActionButton drawLineButton = findViewById(R.id.draw_buttom);
        drawLineButton.setOnClickListener(new View.OnClickListener() {
            @Override

            public void onClick(View view) {
                //draw a line bteween the two Anchors, if there are exactly two anchros
                Log.d(TAG,"drawing line");
                if (numberOfAnchors == 2 ) {
                    drawLine(anchorNodeList.get(0), anchorNodeList.get(1));
                }
            }
        });

        //Add a listener for the delete button
        FloatingActionButton deleteButton = findViewById(R.id.delete_buttom);
        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override

            public void onClick(View view) {
                //Delete the Anchor if it exists
                Log.d(TAG,"Deleteing anchor");
                int currentAnchorIndex;
                if (numberOfAnchors < 1 ) {
                    Toast.makeText(LineViewMainActivity.this, "All nodes deleted", Toast.LENGTH_SHORT).show();
                    return;
                }
                removeAnchorNode(currentSelectedAnchorNode);
                currentSelectedAnchorNode = null;

                //Remove the line if it exists also
                removeLine(nodeForLine);
            }
        });
    }

    private void handleOnTouch(HitTestResult hitTestResult, MotionEvent motionEvent) {
        Log.d(TAG,"handleOnTouch");
        // First call ArFragment's listener to handle TransformableNodes.
        arFragment.onPeekTouch(hitTestResult, motionEvent);

        //We are only interested in the ACTION_UP events - anything else just return
        if (motionEvent.getAction() != MotionEvent.ACTION_UP) {
            return;
        }

        // Check for touching a Sceneform node
        if (hitTestResult.getNode() != null) {
            Log.d(TAG,"handleOnTouch hitTestResult.getNode() != null");
            //Toast.makeText(LineViewMainActivity.this, "hitTestResult is not null: ", Toast.LENGTH_SHORT).show();
            Node hitNode = hitTestResult.getNode();

            if (hitNode.getRenderable() == andyRenderable) {
                //Toast.makeText(LineViewMainActivity.this, "We've hit Andy!!", Toast.LENGTH_SHORT).show();
                //First make the current (soon to be not current) selected node not highlighted
                if (currentSelectedAnchorNode != null) {
                    currentSelectedAnchorNode.setRenderable(andyRenderable);
                }
                //Now highlight the new current selected node
                ModelRenderable highlightedAndyRenderable = andyRenderable.makeCopy();
                highlightedAndyRenderable.getMaterial().setFloat3("baseColorTint", new Color(android.graphics.Color.rgb(255,0,0)));
                hitNode.setRenderable(highlightedAndyRenderable);
                currentSelectedAnchorNode = (AnchorNode) hitNode;
            }
            return;
        } else{
            // Place the anchor 1m in front of the camera. Make sure we are not at maximum anchor first.
            Log.d(TAG,"adding Andy in fornt of camera");
            if (numberOfAnchors < MAX_ANCHORS) {
                Frame frame = arFragment.getArSceneView().getArFrame();
                int currentAnchorIndex = numberOfAnchors;
                Session session = arFragment.getArSceneView().getSession();
                Anchor newMarkAnchor = session.createAnchor(
                        frame.getCamera().getPose()
                                .compose(Pose.makeTranslation(0, 0, -1f))
                                .extractTranslation());
                AnchorNode addedAnchorNode = new AnchorNode(newMarkAnchor);
                addedAnchorNode.setRenderable(andyRenderable);
                addAnchorNode(addedAnchorNode);
                currentSelectedAnchorNode = addedAnchorNode;
            } else {
                Log.d(TAG,"MAX_ANCHORS exceeded");
            }
        }

    }

  /**
   * Returns false and displays an error message if Sceneform can not run, true if Sceneform can run
   * on this device.
   *
   * <p>Sceneform requires Android N on the device as well as OpenGL 3.0 capabilities.
   *
   * <p>Finishes the activity if Sceneform can not run
   */
  public static boolean checkIsSupportedDeviceOrFinish(final Activity activity) {
    if (Build.VERSION.SDK_INT < VERSION_CODES.N) {
      Log.e(TAG, "Sceneform requires Android N or later");
      Toast.makeText(activity, "Sceneform requires Android N or later", Toast.LENGTH_LONG).show();
      activity.finish();
      return false;
    }
    String openGlVersionString =
        ((ActivityManager) activity.getSystemService(Context.ACTIVITY_SERVICE))
            .getDeviceConfigurationInfo()
            .getGlEsVersion();
    if (Double.parseDouble(openGlVersionString) < MIN_OPENGL_VERSION) {
      Log.e(TAG, "Sceneform requires OpenGL ES 3.0 later");
      Toast.makeText(activity, "Sceneform requires OpenGL ES 3.0 or later", Toast.LENGTH_LONG)
          .show();
      activity.finish();
      return false;
    }
    return true;
  }

    private void removeAnchorNode(AnchorNode nodeToremove) {
        //Remove an anchor node
        if (nodeToremove != null) {
            arFragment.getArSceneView().getScene().removeChild(nodeToremove);
            anchorNodeList.remove(nodeToremove);
            nodeToremove.getAnchor().detach();
            nodeToremove.setParent(null);
            nodeToremove = null;
            numberOfAnchors--;
            //Toast.makeText(LineViewMainActivity.this, "Test Delete - markAnchorNode removed", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(LineViewMainActivity.this, "Delete - no node selected! Touch a node to select it.", Toast.LENGTH_SHORT).show();
        }
    }

    private void removeLine(Node lineToRemove) {
      //remove the line
        Log.e(TAG, "removeLine");
        if (lineToRemove != null) {
            Log.e(TAG, "removeLine lineToRemove is not mull");
            arFragment.getArSceneView().getScene().removeChild(lineToRemove);
            lineToRemove.setParent(null);
            lineToRemove = null;
        }
    }

    private void addAnchorNode(AnchorNode nodeToAdd) {
        //Add an anchor node
        nodeToAdd.setParent(arFragment.getArSceneView().getScene());
        anchorNodeList.add(nodeToAdd);
        numberOfAnchors++;
    }

    private AnchorNode moveRenderable(AnchorNode markAnchorNodeToMove, Pose newPoseToMoveTo) {
        //Move a renderable to a new pose
        if (markAnchorNodeToMove != null) {
            arFragment.getArSceneView().getScene().removeChild(markAnchorNodeToMove);
            anchorNodeList.remove(markAnchorNodeToMove);
        } else {
            Log.d(TAG,"moveRenderable - markAnchorNode was null, the little £$%^...");
            return null;
        }
        Frame frame = arFragment.getArSceneView().getArFrame();
        Session session = arFragment.getArSceneView().getSession();
        Anchor markAnchor = session.createAnchor(newPoseToMoveTo.extractTranslation());
        AnchorNode newMarkAnchorNode = new AnchorNode(markAnchor);
        newMarkAnchorNode.setRenderable(andyRenderable);
        newMarkAnchorNode.setParent(arFragment.getArSceneView().getScene());
        anchorNodeList.add(newMarkAnchorNode);

        //Delete the line if it is drawn
        removeLine(nodeForLine);

        return newMarkAnchorNode;
    }

    private void drawLine(AnchorNode node1, AnchorNode node2) {
      //Draw a line between two AnchorNodes (adapted from https://stackoverflow.com/a/52816504/334402)
        Log.d(TAG,"drawLine");
        Vector3 point1, point2;
        point1 = node1.getWorldPosition();
        point2 = node2.getWorldPosition();


        //First, find the vector extending between the two points and define a look rotation
        //in terms of this Vector.
        final Vector3 difference = Vector3.subtract(point1, point2);
        final Vector3 directionFromTopToBottom = difference.normalized();
        final Quaternion rotationFromAToB =
                Quaternion.lookRotation(directionFromTopToBottom, Vector3.up());
        MaterialFactory.makeOpaqueWithColor(getApplicationContext(), new Color(0, 255, 244))
                .thenAccept(
                        material -> {
                            /* Then, create a rectangular prism, using ShapeFactory.makeCube() and use the difference vector
                                   to extend to the necessary length.  */
                            Log.d(TAG,"drawLine insie .thenAccept");
                            ModelRenderable model = ShapeFactory.makeCube(
                                    new Vector3(.01f, .01f, difference.length()),
                                    Vector3.zero(), material);
                            /* Last, set the world rotation of the node to the rotation calculated earlier and set the world position to
                                   the midpoint between the given points . */
                            Anchor lineAnchor = node2.getAnchor();
                            nodeForLine = new Node();
                            nodeForLine.setParent(node1);
                            nodeForLine.setRenderable(model);
                            nodeForLine.setWorldPosition(Vector3.add(point1, point2).scaled(.5f));
                            nodeForLine.setWorldRotation(rotationFromAToB);
                        }
                );

    }
}
