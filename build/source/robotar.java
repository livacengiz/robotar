import processing.core.*; 
import processing.data.*; 
import processing.event.*; 
import processing.opengl.*; 

import ddf.minim.*; 
import ddf.minim.signals.*; 
import ddf.minim.analysis.*; 
import javax.sound.sampled.*; 
import KinectPV2.*; 
import KinectPV2.KJoint; 
import java.util.Random; 

import java.util.HashMap; 
import java.util.ArrayList; 
import java.io.File; 
import java.io.BufferedReader; 
import java.io.PrintWriter; 
import java.io.InputStream; 
import java.io.OutputStream; 
import java.io.IOException; 

public class robotar extends PApplet {

// LIBRARIES







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

int red = color(113, 4, 4);
int black = color(0, 0, 0);
int white = color(255, 255, 255);
int blue = color(0, 0, 255);
int green = color(0, 255, 0);
int firered = color(255, 0, 77);
int saks = color(18, 3, 152);
int aspect = 4;



// TRYIT
Repeller repeller;

public void setup() {
	 //size(1024, 768, P3D);
	
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
	// kinect.enableSkeleton3DMap(true);
     //kinect.enableDepthMaskImg(true);

    kinect.init();
	background(0);
	noStroke();
	
	ps = new ParticleSystem();
	// colorMode(HSB, 255, 255, 255, 100);
}

public void draw( ) {
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
		if (in.left.get(i)*50 > 0.2f) {
			streetBuffer = in.left.get(i)*300;
		}
	}
	float mappedStreetBuffer = map(streetBuffer, 0, 15, 0, width);
	ps.addParticle(streetBuffer*2, new PVector(random(0, width), random(10,height-10)));
	streetBuffer = 0;
	// PVector gravity = new PVector(0,0.1);
	// ps.applyForce(gravity);
	// repeller.display();
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

public void mousePressed() {
  println(frameRate);
}


// function calls the update of a mover and gives it a random choice between two joints
// this creates the bone effect
public void updateMoverToJoints(int[] bone, Mover[] JointMovers, KJoint[] joints) {
  int limit = (bone.length > 2) ? bone[2] : numParticles;

  //loop through the movers passed in
  for (int a = 0; a < limit; a++) {

    //random number between 1 and 10
    int num = PApplet.parseInt(random(1, 10));

    //check if random number is even. set the the joint
    int joint = (num%2 == 0) ? bone[0] : bone[1];
		// ps.applyRepeller(JointMovers[a]);
    //update and display the movers
    JointMovers[a].update(joints[joint]);
    // JointMovers[a].checkEdges();
    JointMovers[a].display(true, mappedRobotar);
  }
}

public void updateMoversToRandom(){
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
public void setupParticles() {

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


class Mover {

  PVector location;
  PVector velocity;
  PVector acceleration;
  float topspeed;
  float noneTrackScale;
  float trackScale;
  private float tx;
  private float ty;
  float x;
  float y;
  float colour;
  // maximum length of line
  float lengthRange;
  float edgePadding;
  Random r = new Random();

  float G = 350;

  //constructor
  Mover(float tSpeed, float tScale, float ntScale, float range, float pad) {

    //initial settings per mover
    location = new PVector(random(width), random(height));
    velocity = new PVector(0, 0);

    // initial movement params
    topspeed = tSpeed;
    noneTrackScale = ntScale;
    trackScale = tScale;
    lengthRange = range;
    edgePadding = pad;

    //start at a different point on the noise wave
    tx = round(r.nextInt(10000));
    ty =  round(r.nextInt(10000));

    println("TX = " + tx + "TY = " + ty);

    colour = random(1, 3);
  }

  public void update(KJoint joint) {

    float desiredX;
    float desiredY;
    float maxSpeed;
    float scale;

    // null joint passed in == no skeleton
    if (joint == null) {

      // get x and y from noise wave = random position on the screen
      desiredX = map(noise(tx), 0, 1, width*-4, width*5);
      desiredY = map(noise(ty), 0, 1, height*-4, height*5);

      //slow down the topspeed
      maxSpeed = topspeed*0.1f;
      scale = noneTrackScale;
    } else {

      // use the joint passed in the find it's X and Y coordinates
      desiredX = joint.getX();
      desiredY = joint.getY();
      maxSpeed = topspeed;
      scale = trackScale/2;

      //if a joint isn't tracked/off screen, it has a postition of -infinity
      if (desiredX == Float.NEGATIVE_INFINITY || desiredY == Float.NEGATIVE_INFINITY) {

        desiredX = map(noise(tx), 0, 1, width, width);
        desiredY = map(noise(ty), 0, 1, height, height);
      }
    }

    // location to follow
    PVector desiredVector = new PVector(desiredX, desiredY);

    // Find vector pointing towards location
    PVector dir = PVector.sub(desiredVector, location);

    dir.normalize();     // Normalize
    dir.mult(scale);       // Scale
    acceleration = dir;  // Set to acceleration

    // Motion 101!  Velocity changes by acceleration.  Location changes by velocity.
    velocity.add(acceleration);
    velocity.limit(maxSpeed);
    location.add(velocity);

    // move noise wave along to get next random number
    tx += 0.0001f;
    ty += 0.0001f;

    // just in case these get too high, might not be needed
    if (tx > 2100000000 ){
      tx = round(r.nextInt(10000));
      println("TX = " + tx);
    }
    if (ty > 2100000000 ) {
      ty = round(r.nextInt(10000));
      println("TY = " + ty);
    }
  }

  //draw the mover on screen
  public void display(boolean isJoint, float robotarFFT ) {
    // line colour
    if (isJoint) {
      stroke(255, robotarFFT);
      strokeWeight(robotarFFT/3);
    } else {
      stroke(white, 100);
      strokeWeight(1);
    }


    // line weight
    // strokeWeight(map(noise(tx), 0, 1, -lengthRange, lengthRange));


    // random x and y to use for line length and location based on noise
    // should be smoother than random above
    float randX = map(noise(tx), 0, 1, -lengthRange, lengthRange);
    float randY = map(noise(ty), 0, 1, -lengthRange, lengthRange);

    // draw from current location to the random x and y
    line(location.x, location.y, location.x+randX, location.y+randY);

    // box instead of line
    //pushMatrix();
    //  translate(location.x,  location.y, 0);
    //  rotateX(radians(20));
    //  rotateY(radians(10));
    //  rotateZ(noise(tx));
    //  fill(49, 255, 183);
    //  noStroke();
    //  box(30);
    //popMatrix();

  }

  public PVector repel(Particle p) {
    PVector dir = PVector.sub(location, p.position);
    float d = dir.mag();
    dir.normalize();
    d = constrain(d, 5, 100);
    float force = -1 * G / (d * d);
    dir.mult(force);
    return dir;

  }

  // if mover goes off the screen, bring it back
  public void checkEdges() {

    //check width
    if (location.x > width + edgePadding) {
      location.x = 0 - edgePadding;
    } else if (location.x < 0 - edgePadding) {
      location.x = width + edgePadding;
    }

    //check height
    if (location.y > height + edgePadding) {
      location.y = 0 - edgePadding;
    } else if (location.y < 0 - edgePadding) {
      location.y = height + edgePadding;
    }
  }
}
class Particle {
	PVector position;
	PVector velocity;
	PVector acceleration;
	float lifespan;
	float mass = 20;

	Particle(PVector l) {
		acceleration = new PVector(0.008f, 0.02f);
		velocity = new PVector(random(-1, 1), random(0, 1));
		position = l.copy();
		lifespan = 255.0f;
	}

	public void run( ) {
		update();
		display();
	}

	public void applyForce(PVector force) {
		PVector f = force.get();
		f.div(mass);
		acceleration.add(f);
	}

	public void update() {
		velocity.add(acceleration);
		position.add(velocity);
		lifespan -= 1.0f;
	}

	public void display() {
		// stroke(255,0,0,  lifespan);
		fill(firered, lifespan);
		// fill(204, lifespan);
		noStroke();
		rect(position.x,position.y,60,8);
	}

	public boolean isDead() {
		if (lifespan < 0.0f) {
			return true;
		} else {
			return false;
		}
	}
}
class ParticleSystem {

	ArrayList<Particle> particles;
	ArrayList<Mover> movers;

	ParticleSystem() {
		particles = new ArrayList<Particle>();
	}

	public void addParticle(float s, PVector position) {
		for (int i = 0; i < s; i ++) {
			particles.add(new Particle(position));
		}
	}

	public void applyForce(PVector f) {
		for (Particle p: particles) {
			p.applyForce(f);
		}
	}

	public void applyRepeller(Mover m ) {
		for (Particle p: particles) {
			PVector force = m.repel(p);
			p.applyForce(force);
		}
	}

	public void run() {
		for (int i = particles.size()-1; i >= 0; i--) {
			Particle p = particles.get(i);
			p.run();
			if (p.isDead()) {
				particles.remove(i);
			}
		}
	}
}
class Repeller {

  float G = 350;

  PVector position;
  float r = 10;

  Repeller(float x, float y) {
    position = new PVector(x, y);
  }

  public void display() {
    stroke(0);
    fill(255);
    ellipse(position.x, position.y, 50,50);
  }

  public PVector repel(Particle p) {
    PVector dir = PVector.sub(position, p.position);
    float d = dir.mag();
    dir.normalize();
    d = constrain(d, 5, 100);
    float force = -1 * G / (d * d);
    dir.mult(force);
    return dir;

  }

}
static int HEAD = 1;
static int NECK = 2;
static int SPINE_MID = 3;
static int SPINE_BASE = 4;
static int SHOULDER_RIGHT = 5;
static int SHOULDER_LEFT = 6;
static int HIP_RIGHT = 7;
static int HIP_LEFT = 8;
static int UPPER_ARM_RIGHT = 9;
static int LOWER_ARM_RIGHT = 10;
static int WRIST_RIGHT = 11;
static int HAND_RIGHT = 12;
static int THUMB_RIGHT = 13;
static int UPPER_ARM_LEFT = 14;
static int LOWER_ARM_LEFT = 15;
static int WRIST_LEFT = 16;
static int HAND_LEFT = 17;
static int THUMB_LEFT = 18;
static int UPPER_LEG_RIGHT = 19;
static int LOWER_LEG_RIGHT = 20;
static int FOOT_RIGHT = 21;
static int UPPER_LEG_LEFT = 22;
static int LOWER_LEG_LEFT = 23;
static int FOOT_LEFT = 24;

//extra fake bones
static int BODY_LEFT = 25;
static int BODY_RIGHT = 26;
static int BODY_CROSS_LEFT = 27;
static int BODY_CROSS_RIGHT = 28;

static float LIMIT_FACTOR = 0.5f;
static int PARTICLE_LIMIT = round(numParticles * LIMIT_FACTOR);


static int[][] bones = {

  //head
  { KinectPV2.JointType_Head, KinectPV2.JointType_Neck },

  //neck
  { KinectPV2.JointType_Neck, KinectPV2.JointType_SpineShoulder },

  //spine
  { KinectPV2.JointType_SpineShoulder, KinectPV2.JointType_SpineMid },
  { KinectPV2.JointType_SpineMid, KinectPV2.JointType_SpineBase },

  //shoulders
  { KinectPV2.JointType_SpineShoulder, KinectPV2.JointType_ShoulderRight, PARTICLE_LIMIT },
  { KinectPV2.JointType_SpineShoulder, KinectPV2.JointType_ShoulderLeft, PARTICLE_LIMIT },

  //hips
  { KinectPV2.JointType_SpineBase, KinectPV2.JointType_HipRight, PARTICLE_LIMIT },
  { KinectPV2.JointType_SpineBase, KinectPV2.JointType_HipLeft, PARTICLE_LIMIT },

  //right arm
  { KinectPV2.JointType_ShoulderRight, KinectPV2.JointType_ElbowRight },
  { KinectPV2.JointType_ElbowRight, KinectPV2.JointType_WristRight },
  { KinectPV2.JointType_WristRight, KinectPV2.JointType_HandRight, PARTICLE_LIMIT },
  { KinectPV2.JointType_HandRight, KinectPV2.JointType_HandTipRight, PARTICLE_LIMIT },
  { KinectPV2.JointType_WristRight, KinectPV2.JointType_ThumbRight, PARTICLE_LIMIT },

  //left arm
  { KinectPV2.JointType_ShoulderLeft, KinectPV2.JointType_ElbowLeft },
  { KinectPV2.JointType_ElbowLeft, KinectPV2.JointType_WristLeft },
  { KinectPV2.JointType_WristLeft, KinectPV2.JointType_HandLeft, PARTICLE_LIMIT },
  { KinectPV2.JointType_HandLeft, KinectPV2.JointType_HandTipLeft, PARTICLE_LIMIT },
  { KinectPV2.JointType_WristLeft, KinectPV2.JointType_ThumbLeft, PARTICLE_LIMIT },

  //right leg
  { KinectPV2.JointType_HipRight, KinectPV2.JointType_KneeRight },
  { KinectPV2.JointType_KneeRight, KinectPV2.JointType_AnkleRight },
  { KinectPV2.JointType_AnkleRight, KinectPV2.JointType_FootRight },

  //left leg
  { KinectPV2.JointType_HipLeft, KinectPV2.JointType_KneeLeft },
  { KinectPV2.JointType_KneeLeft, KinectPV2.JointType_AnkleLeft },
  { KinectPV2.JointType_AnkleLeft, KinectPV2.JointType_FootLeft },

  //extra body joints that make the cross
  { KinectPV2.JointType_HipRight, KinectPV2.JointType_ShoulderRight },
  { KinectPV2.JointType_HipLeft, KinectPV2.JointType_ShoulderLeft },
  // { KinectPV2.JointType_HipLeft, KinectPV2.JointType_ShoulderRight },
  // { KinectPV2.JointType_HipRight, KinectPV2.JointType_ShoulderLeft }

};
  public void settings() { 	fullScreen(P3D); 	smooth(); }
  static public void main(String[] passedArgs) {
    String[] appletArgs = new String[] { "robotar" };
    if (passedArgs != null) {
      PApplet.main(concat(appletArgs, passedArgs));
    } else {
      PApplet.main(appletArgs);
    }
  }
}
