import ddf.minim.*;
import ddf.minim.signals.*;
import javax.sound.sampled.*;
 import KinectPV2.*;
 import KinectPV2.KJoint;

Minim minim;
Minim minim1;
AudioInput in;
AudioInput robotarInput;
Mixer.Info[] mixerInfo;

ParticleSystem ps;
float s;
float robotar;
float t = 0;

import javax.sound.sampled.*;

 KinectPV2 kinect;

//==========COLORS

color red = color(113, 4, 4);
color black = color(0, 0, 0);
color white = color(255, 255, 255);
color blue = color(0, 0, 255);
color green = color(0, 255, 0);
int aspect = 4;

void setup() {
	 //size(1024, 768, P3D);
	fullScreen(P3D);

	minim = new Minim(this);
	minim1 = new Minim(this);

	mixerInfo = AudioSystem.getMixerInfo();

	for(int i = 0; i < mixerInfo.length; i++)
	{println(i + " = " + mixerInfo[i].getName());}

	minim.setInputMixer(AudioSystem.getMixer(mixerInfo[6]));
	in = minim.getLineIn(Minim.MONO);

	minim1.setInputMixer(AudioSystem.getMixer(mixerInfo[4]));
    robotarInput = minim1.getLineIn(Minim.MONO);

	 kinect = new KinectPV2(this);
	
	 //kinect.enableBodyTrackImg(true);
  kinect.enableSkeletonColorMap(true);
     //kinect.enableDepthMaskImg(true);
	
     kinect.init();

	background(0);
	noStroke();
	smooth();
	ps = new ParticleSystem();
	colorMode(HSB, 255, 255, 255, 255);

}

void draw( ) {
	background(0);
	in.disableMonitoring();
	robotarInput.disableMonitoring();
	// fft.forward( in.mix );
	// fft1.forward( robotarInput.mix );

pushMatrix();
fill(255, 0, 255);
textSize(32);
stroke(255);
text(frameRate, 200, 30); 

popMatrix();


  ArrayList<KSkeleton> skeletonArray =  kinect.getSkeletonColorMap();

  //individual JOINTS
  for (int i = 0; i < skeletonArray.size(); i++) {
    KSkeleton skeleton = (KSkeleton) skeletonArray.get(i);
    if (skeleton.isTracked()) {
      KJoint[] joints = skeleton.getJoints();

      color col  = skeleton.getIndexColor();
      fill(col);
      stroke(col);
      drawBody(joints);

      //draw different color for each hand state
      drawHandState(joints[KinectPV2.JointType_HandRight]);
      drawHandState(joints[KinectPV2.JointType_HandLeft]);
    }
  }

  fill(255, 0, 0);
  text(frameRate, 50, 50);


//	 ArrayList<PImage> bodyTrackList = kinect.getBodyTrackUser();
	
//	 for (int i = 0; i < bodyTrackList.size(); i++) {
//	   PImage bodyTrackImg = (PImage)bodyTrackList.get(i);
//	   if (i <= 2) {
//      imageMode(CENTER);
//  	 	image(bodyTrackImg, width/2, height-120, 320, 240);
//      }
//	 }


	for(int i = 0; i < in.bufferSize() -1; i++){
		s = in.left.get(i)*200;
	}
	if (s > 0.3) {
		ps.addParticle(s, new PVector(random(0, width), random(10,height-10)));
	}
	ps.run();


	for(int i = 0; i < robotarInput.bufferSize() -1; i++){
		if (robotarInput.left.get(i)*200  > 0.3 && robotarInput.left.get(i)*200 < 14)  {
          robotar = robotarInput.left.get(i)*200;
      }
	}
pushMatrix();
	beginShape();
    float mappedRobotar = map(robotar, 0, 12, 0, 1);
    float rt = constrain(mappedRobotar, 0, 1);
		stroke(255);
    
		strokeWeight(4);
		smooth();
		translate(width/2,height/2);
		for (float theta = 0; theta <= 2 * PI; theta += 0.01) {
			float rad = r(theta,
				2, //a
        1, //b
				1, // m
				1, // n1
				sin(t) * 0.05 + 0.05, //n2
				cos(t) * 0.05 + 0.05 //n3
			);
			float x = rad * cos(theta) * 150;
			float y = rad * sin(theta) * 150;
			vertex(x,y);
		}
//println(mappedRobotar);
	endShape();
popMatrix();

	t = t + 0.00005 + rt;
  
  if (t > height/2) {
    t = 0;
  }



}

// void keyPressed() {
// 	switch (key) {
// 		case 1: aspect = 1;
// 				break;
// 		case 2: aspect = 2;
// 				break;
// 		case 3: aspect = 3;
// 				break;
// 		case 4: aspect = 4;
// 				break;
// 		case 5: aspect = 5;
// 				break;
// 		case 6: aspect = 6;
// 				break;
// 		case 7: aspect = 7;
// 				break;
// 		case 8: aspect = 8;
// 				break;
// 		case 9: aspect = 9;
// 				break;
// 		default: aspect = 4;
// 				break;
// 	}
// }



float r(float theta, float a, float b, float m, float n1, float n2, float n3) {
	return pow(pow(abs(cos(m * theta / 4.0) / a), n2) + pow(abs(sin(m * theta / 4.0) / b), n3), 1.0 / n1);
	// return 1;
}

void mousePressed() {
  println(frameRate);
}


//DRAW BODY
void drawBody(KJoint[] joints) {
  drawBone(joints, KinectPV2.JointType_Head, KinectPV2.JointType_Neck);
  drawBone(joints, KinectPV2.JointType_Neck, KinectPV2.JointType_SpineShoulder);
  drawBone(joints, KinectPV2.JointType_SpineShoulder, KinectPV2.JointType_SpineMid);
  drawBone(joints, KinectPV2.JointType_SpineMid, KinectPV2.JointType_SpineBase);
  drawBone(joints, KinectPV2.JointType_SpineShoulder, KinectPV2.JointType_ShoulderRight);
  drawBone(joints, KinectPV2.JointType_SpineShoulder, KinectPV2.JointType_ShoulderLeft);
  drawBone(joints, KinectPV2.JointType_SpineBase, KinectPV2.JointType_HipRight);
  drawBone(joints, KinectPV2.JointType_SpineBase, KinectPV2.JointType_HipLeft);

  // Right Arm
  drawBone(joints, KinectPV2.JointType_ShoulderRight, KinectPV2.JointType_ElbowRight);
  drawBone(joints, KinectPV2.JointType_ElbowRight, KinectPV2.JointType_WristRight);
  drawBone(joints, KinectPV2.JointType_WristRight, KinectPV2.JointType_HandRight);
  drawBone(joints, KinectPV2.JointType_HandRight, KinectPV2.JointType_HandTipRight);
  drawBone(joints, KinectPV2.JointType_WristRight, KinectPV2.JointType_ThumbRight);

  // Left Arm
  drawBone(joints, KinectPV2.JointType_ShoulderLeft, KinectPV2.JointType_ElbowLeft);
  drawBone(joints, KinectPV2.JointType_ElbowLeft, KinectPV2.JointType_WristLeft);
  drawBone(joints, KinectPV2.JointType_WristLeft, KinectPV2.JointType_HandLeft);
  drawBone(joints, KinectPV2.JointType_HandLeft, KinectPV2.JointType_HandTipLeft);
  drawBone(joints, KinectPV2.JointType_WristLeft, KinectPV2.JointType_ThumbLeft);

  // Right Leg
  drawBone(joints, KinectPV2.JointType_HipRight, KinectPV2.JointType_KneeRight);
  drawBone(joints, KinectPV2.JointType_KneeRight, KinectPV2.JointType_AnkleRight);
  drawBone(joints, KinectPV2.JointType_AnkleRight, KinectPV2.JointType_FootRight);

  // Left Leg
  drawBone(joints, KinectPV2.JointType_HipLeft, KinectPV2.JointType_KneeLeft);
  drawBone(joints, KinectPV2.JointType_KneeLeft, KinectPV2.JointType_AnkleLeft);
  drawBone(joints, KinectPV2.JointType_AnkleLeft, KinectPV2.JointType_FootLeft);

  drawJoint(joints, KinectPV2.JointType_HandTipLeft);
  drawJoint(joints, KinectPV2.JointType_HandTipRight);
  drawJoint(joints, KinectPV2.JointType_FootLeft);
  drawJoint(joints, KinectPV2.JointType_FootRight);

  drawJoint(joints, KinectPV2.JointType_ThumbLeft);
  drawJoint(joints, KinectPV2.JointType_ThumbRight);

  drawJoint(joints, KinectPV2.JointType_Head);
}

//draw joint
void drawJoint(KJoint[] joints, int jointType) {
  pushMatrix();
  translate(joints[jointType].getX(), joints[jointType].getY(), joints[jointType].getZ());
  ellipse(0, 0, 25, 25);
  popMatrix();
}

//draw bone
void drawBone(KJoint[] joints, int jointType1, int jointType2) {
  pushMatrix();
  translate(joints[jointType1].getX(), joints[jointType1].getY(), joints[jointType1].getZ());
  ellipse(0, 0, 25, 25);
  popMatrix();
  line(joints[jointType1].getX(), joints[jointType1].getY(), joints[jointType1].getZ(), joints[jointType2].getX(), joints[jointType2].getY(), joints[jointType2].getZ());
}

//draw hand state
void drawHandState(KJoint joint) {
  noStroke();
  handState(joint.getState());
  pushMatrix();
  translate(joint.getX(), joint.getY(), joint.getZ());
  ellipse(0, 0, 70, 70);
  popMatrix();
}

/*
Different hand state
 KinectPV2.HandState_Open
 KinectPV2.HandState_Closed
 KinectPV2.HandState_Lasso
 KinectPV2.HandState_NotTracked
 */
void handState(int handState) {
  switch(handState) {
  case KinectPV2.HandState_Open:
    fill(0, 255, 0);
    break;
  case KinectPV2.HandState_Closed:
    fill(255, 0, 0);
    break;
  case KinectPV2.HandState_Lasso:
    fill(0, 0, 255);
    break;
  case KinectPV2.HandState_NotTracked:
    fill(255, 255, 255);
    break;
  }
}
