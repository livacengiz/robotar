class ParticleSystem {

	ArrayList<Particle> particles;

	ParticleSystem() {
		particles = new ArrayList<Particle>();
	}

	void addParticle(float s, PVector position) {
		for (int i = 0; i < s; i ++) {
			particles.add(new Particle(position));
		}
	}

	void run() {
		for (int i = particles.size()-1; i >= 0; i--) {
			Particle p = particles.get(i);
			p.run();
			if (p.isDead()) {
				particles.remove(i);
			}
		}
	}
}
