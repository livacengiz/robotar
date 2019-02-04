// LIBRARIES
import ddf.minim.*;
import ddf.minim.signals.*;
import ddf.minim.analysis.*;
import javax.sound.sampled.*;
import KinectPV2.*;
import KinectPV2.KJoint;

// ========== KINECT ======
KinectPV2 kinect;


// ========== SOUND ======
Minim minim;
Minim minim1;
AudioInput in;
AudioInput robotarInput;
FFT robotarFFT;
Mixer.Info[] mixerInfo;



ParticleSystem ps;
float s;
float robotar;
float mappedRobotar = 0;
float t = 0;

float streetBuffer;

// ========MOVER======

//skeleton variables
int numBones = bones.length;
//int numJoints = 26;
static int numParticles = 400;

// global particle vars
float topspeed = 12;
float noneTrackScale = 1;
float trackScale = 6;
float lengthRange = 30;
float edgePadding = 40;


Mover[][] movers;

//==========COLORS

color red = color(113, 4, 4);
color black = color(0, 0, 0);
color white = color(255, 255, 255);
color blue = color(0, 0, 255);
color green = color(0, 255, 0);
color firered = color(255, 0, 77);
color saks = color(18, 3, 152);
int aspect = 4;



void setup() {
	 //size(1024, 768, P3D);
	fullScreen(P3D);
	setupParticles();
	// MINIM SETUP
	minim = new Minim(this);
	minim1 = new Minim(this);
	mixerInfo = AudioSystem.getMixerInfo();
	for(int i = 0; i < mixerInfo.length; i++) {
		println(i + " = " + mixerInfo[i].getName());
	}
	minim.setInputMixer(AudioSystem.getMixer(mixerInfo[6]));
	in = minim.getLineIn(Minim.MONO);

	minim1.setInputMixer(AudioSystem.getMixer(mixerInfo[4]));
  robotarInput = minim1.getLineIn(Minim.MONO);
	robotarFFT = new FFT( robotarInput.bufferSize(), robotarInput.sampleRate() );
	robotarFFT.linAverages( 50 );

	// KINECT SETUP
	kinect = new KinectPV2(this);
	// kinect.enableBodyTrackImg(true);
  kinect.enableSkeletonColorMap(true);
     //kinect.enableDepthMaskImg(true);

    kinect.init();
	background(0);
	noStroke();
	smooth();
	ps = new ParticleSystem();
	// colorMode(HSB, 255, 255, 255, 100);
}

void draw( ) {
	background(0);
	in.disableMonitoring();
	robotarInput.disableMonitoring();
	robotarFFT.forward(robotarInput.left);

	// frameRate
	pushMatrix();
		fill(255, 0, 255);
		textSize(32);
		stroke(255);
		text(frameRate, 200, 30);
	popMatrix();

	// ====================================== SOUND START ============================

	// STREET SOUND
	for(int i = 0; i < in.bufferSize() -1; i++){
		if (in.left.get(i)*50 > 0.2) {
			streetBuffer = in.left.get(i)*300;
		}
	}
	float mappedStreetBuffer = map(streetBuffer, 0, 15, 0, width);
	ps.addParticle(streetBuffer*2, new PVector(random(0, width), random(10,height-10)));
	streetBuffer = 0;
	ps.run();


	// ROBOTAR SOUND

	for(int i = 0; i < robotarFFT.specSize(); i++){
		mappedRobotar = robotarFFT.getBand(i)*255;
	}
	mappedRobotar = constrain(mappedRobotar, 10, 100);
	// println(mappedRobotar);

	// ====================================== SOUND END ============================


	// ====================================== SKELETON START ============================

	ArrayList<KSkeleton> skeletonArray = kinect.getSkeletonColorMap();

	for (int i = 0; i < skeletonArray.size(); i++) {

      //get the skeleton
      KSkeleton skeleton = (KSkeleton) skeletonArray.get(i);

      //check if it is being tracked and the first person
      if (skeleton.isTracked() && i == 0) {

        //get all the joints
        KJoint[] joints = skeleton.getJoints();

        // loop through the bones and update movers to each
        for(int x = 0; x < bones.length; x++) {
          updateMoverToJoints(bones[x], movers[x], joints);
        }
      }
    }

	//if there is no skeleton
    if (skeletonArray.size() == 0) {

      //send movers to random location
      updateMoversToRandom();

      // stops the Z plane being rendered, known hack for 2D on top of 3D
      //hint(DISABLE_DEPTH_TEST);

    }

	// ====================================== SKELETON END ============================
}

void mousePressed() {
  println(frameRate);
}


// function calls the update of a mover and gives it a random choice between two joints
// this creates the bone effect
void updateMoverToJoints(int[] bone, Mover[] JointMovers, KJoint[] joints) {
  int limit = (bone.length > 2) ? bone[2] : numParticles;

  //loop through the movers passed in
  for (int a = 0; a < limit; a++) {

    //random number between 1 and 10
    int num = int(random(1, 10));

    //check if random number is even. set the the joint
    int joint = (num%2 == 0) ? bone[0] : bone[1];
    //update and display the movers
    JointMovers[a].update(joints[joint]);
    // JointMovers[a].checkEdges();
    JointMovers[a].display(true, mappedRobotar);
  }
}

void updateMoversToRandom(){
  // loop through bones
  for (int x = 0; x < bones.length; x++) {

    // if there has been a particle limit set on a bone
    int limit = (bones[x].length > 2) ? bones[x][2] : numParticles;

    // loop through movers
    for (int a = 0; a < limit; a++) {

      // send null to show no joint available, mover will pick a random direction
      movers[x][a].update(null);
      movers[x][a].checkEdges();
      movers[x][a].display(false, 0);
    }
  }
}

// initialises all the particles
void setupParticles() {

  // set the amount of particles
  movers = new Mover[numBones][numParticles];

  // Initializing all the particles
  for (int i = 0; i <numBones; i++) {

    // if there has been a particle limit set on a bone
    int limit = (bones[i].length > 2) ? bones[i][2] : numParticles;

    for (int a = 0; a < limit; a++) {

      // for each particle pass in it's params
      movers[i][a] = new Mover(topspeed, trackScale, noneTrackScale, lengthRange, edgePadding);
    }
  }
}
