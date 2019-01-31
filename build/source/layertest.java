import processing.core.*; 
import processing.data.*; 
import processing.event.*; 
import processing.opengl.*; 

import ddf.minim.*; 
import ddf.minim.signals.*; 
import ddf.minim.analysis.*; 
import javax.sound.sampled.*; 

import java.util.HashMap; 
import java.util.ArrayList; 
import java.io.File; 
import java.io.BufferedReader; 
import java.io.PrintWriter; 
import java.io.InputStream; 
import java.io.OutputStream; 
import java.io.IOException; 

public class layertest extends PApplet {





Minim               minim;
AudioInput          record;
FFT         		fft;

ParticleSystem ps;
PVector hBlock;
PVector vBlock;
float s;
float t = 0;


Mixer.Info[] mixerInfo;

//==========COLORS

int red = color(113, 4, 4);
int black = color(0, 0, 0);
int white = color(255, 255, 255);
int blue = color(0, 0, 255);
int green = color(0, 255, 0);
int aspect = 4;

public void setup() {
	// size(480, 720);
	

	minim         = new Minim(this); // import class to analyse sound

	record =  minim.getLineIn(Minim.STEREO, 1024);
	fft = new FFT( record.bufferSize(), record.sampleRate() );

	background(0);
	noStroke();
	
	ps = new ParticleSystem();
	colorMode(HSB, 255, 255, 255, 255);

}

public void draw( ) {
	record.disableMonitoring();
	fft.forward( record.mix );
	background(0);
	for(int i = 0; i < fft.specSize(); i++){
		s = fft.getBand(i)*100;
	}
	if (s > 1) {
		ps.addParticle(s, new PVector(random(0, width), random(10,height-10)));
	}
	ps.run();
	beginShape();
		if (s > 2) {
				fill(red);
		} else {
			fill(black);
		}
		stroke(255);
		strokeWeight(4);
		smooth();
		translate(width/2,height/2);
		float mappedS = map(s, 0,1, 0, 10);
		float st = constrain(mappedS, 2, 8);
		for (float theta = 0; theta <= 2 * PI; theta += 0.01f) {
			float rad = r(theta,
				st / 2.0f, //a
				st / 2.0f, //b
				// 2,
				// 3,
				mouseY / 2, //m
				1,
				sin(t) * 0.5f + 0.5f, //n2
				cos(t) * 0.5f + 0.5f //n3
			);
			float x = rad * cos(theta) * 50;
			float y = rad * sin(theta) * 50;
			vertex(x,y);
		}
	endShape();

	t = s + t + 0.001f;

	if (keyPressed) {
		println(key);
		switch (key) {
			case 1: aspect = 1;
					break;
			case 2: aspect = 2;
					break;
			case 3: aspect = 3;
					break;
			case 4: aspect = 4;
					break;
			case 5: aspect = 5;
					break;
			case 6: aspect = 6;
					break;
			case 7: aspect = 7;
					break;
			case 8: aspect = 8;
					break;
			case 9: aspect = 9;
					break;
			default: aspect = 4;
					break;
		}
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



public float r(float theta, float a, float b, float m, float n1, float n2, float n3) {
	return pow(pow(abs(cos(m * theta / 4.0f) / a), n2) + pow(abs(sin(m * theta / 4.0f) / b), n3), 1.0f / n1);
	// return 1;
}
class Particle {
	PVector position;
	PVector velocity;
	PVector acceleration;
	float lifespan;

	Particle(PVector l) {
		acceleration = new PVector(random(-1, 1), 0.005f);
		velocity = new PVector(random(-1, 1), random(-1, 1));
		position = l.copy();
		lifespan = 255.0f;
	}

	public void run( ) {
		update();
		display();
	}

	public void update() {
		velocity.add(acceleration);
		position.add(velocity);
		lifespan -= 1.0f;
	}

	public void display() {
		// stroke(255,0,0,  lifespan);
		fill(255, 0, lifespan, lifespan);
		// fill(204, lifespan);
		noStroke();
		rect(position.x,position.y,7,100);
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

	ParticleSystem() {
		particles = new ArrayList<Particle>();
	}

	public void addParticle(float s, PVector position) {
		for (int i = 0; i < s; i ++) {
			particles.add(new Particle(position));
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
  public void settings() { 	fullScreen(); 	smooth(); }
  static public void main(String[] passedArgs) {
    String[] appletArgs = new String[] { "--present", "--window-color=#666666", "--hide-stop", "layertest" };
    if (passedArgs != null) {
      PApplet.main(concat(appletArgs, passedArgs));
    } else {
      PApplet.main(appletArgs);
    }
  }
}
