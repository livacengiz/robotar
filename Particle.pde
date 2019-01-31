class Particle {
	PVector position;
	PVector velocity;
	PVector acceleration;
	float lifespan;

	Particle(PVector l) {
		acceleration = new PVector(random(-1, 1), 0.005);
		velocity = new PVector(random(-1, 1), random(-1, 1));
		position = l.copy();
		lifespan = 255.0;
	}

	void run( ) {
		update();
		display();
	}

	void update() {
		velocity.add(acceleration);
		position.add(velocity);
		lifespan -= 1.0;
	}

	void display() {
		// stroke(255,0,0,  lifespan);
		fill(255, 0, lifespan, lifespan);
		// fill(204, lifespan);
		noStroke();
		rect(position.x,position.y,7,100);
	}

	boolean isDead() {
		if (lifespan < 0.0) {
			return true;
		} else {
			return false;
		}
	}
}
