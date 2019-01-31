import ddf.minim.*;
import ddf.minim.signals.*;
import ddf.minim.analysis.*;

Minim               minim;
AudioInput          record;
FFT         		fft;

ParticleSystem ps;
PVector hBlock;
PVector vBlock;
float s;
float t = 0;

import javax.sound.sampled.*;
Mixer.Info[] mixerInfo;

//==========COLORS

color red = color(113, 4, 4);
color black = color(0, 0, 0);
color white = color(255, 255, 255);
color blue = color(0, 0, 255);
color green = color(0, 255, 0);
int aspect = 4;

void setup() {
	// size(480, 720);
	fullScreen();

	minim         = new Minim(this); // import class to analyse sound

	record =  minim.getLineIn(Minim.STEREO, 1024);
	fft = new FFT( record.bufferSize(), record.sampleRate() );

	background(0);
	noStroke();
	smooth();
	ps = new ParticleSystem();
	colorMode(HSB, 255, 255, 255, 255);

}

void draw( ) {
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
		for (float theta = 0; theta <= 2 * PI; theta += 0.01) {
			float rad = r(theta,
				st / 2.0, //a
				st / 2.0, //b
				// 2,
				// 3,
				mouseY / 2, //m
				1,
				sin(t) * 0.5 + 0.5, //n2
				cos(t) * 0.5 + 0.5 //n3
			);
			float x = rad * cos(theta) * 50;
			float y = rad * sin(theta) * 50;
			vertex(x,y);
		}
	endShape();

	t = s + t + 0.001;

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



float r(float theta, float a, float b, float m, float n1, float n2, float n3) {
	return pow(pow(abs(cos(m * theta / 4.0) / a), n2) + pow(abs(sin(m * theta / 4.0) / b), n3), 1.0 / n1);
	// return 1;
}
