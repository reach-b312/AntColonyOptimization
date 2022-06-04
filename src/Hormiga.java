public class Hormiga {

	protected int trailSize;
	protected int[] camino;
	protected boolean visited[];

	public Hormiga(int tourSize) {
		this.trailSize = tourSize;
		this.camino = new int[tourSize];
		this.visited = new boolean[tourSize];
	}

	protected void visitaCiudad(int currentIndex, int city) {
		camino[currentIndex + 1] = city;
		visited[city] = true;
	}

	protected boolean visitado(int i) {
		return visited[i];
	}

	protected double distanciaDeRecorrido(double graph[][]) {
		double length = graph[camino[trailSize - 1]][camino[0]];
		for (int i = 0; i < trailSize - 1; i++) {
			length += graph[camino[i]][camino[i + 1]];
		}
		return length;
	}
        protected double distanciaDeRecorrido(int graph[][]) {
		double length = graph[camino[trailSize - 1]][camino[0]];
		for (int i = 0; i < trailSize - 1; i++) {
			length += graph[camino[i]][camino[i + 1]];
		}
		return length;
	}

	protected void clear() {
		for (int i = 0; i < trailSize; i++)
			visited[i] = false;
	}

}
