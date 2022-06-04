import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.OptionalInt;
import java.util.Random;
import java.util.stream.IntStream;

public class AntColonyOptimization {
    //PARAMETROS
    private double c = 1.0;
    private double alpha = 1;
    private double beta = 5;
    private double evaporacion = 0.5;
    private double Q = 500;
    private double factorHormiga = 0.8;
    private double factorAleatorio = 0.01;

    private int maxIterations = 1000;

    private int numeroDeCiudades;
    private int numeroDeHormigas;
    //private double[][] matriz;
    private double[][] caminos;
    private List<Hormiga> hormigas = new ArrayList<>();
    private Random random = new Random();
    private double[] probabilidades;

    //MATRIZ HARDCODEADA PARA NUESTRO CASO ESPECIFICO
    private int[][] matriz;

    private int currentIndex;

    private int[] mejorOrdenAlRecorrer;
    private double mejorDistanciaDeRecorrido;

    public AntColonyOptimization(/*int noDeCiudades*/) {
        //matriz = generateRandomMatrix(8);
        matriz = inicializarMatriz();
        numeroDeCiudades = matriz.length;
        numeroDeHormigas = (int) (numeroDeCiudades * factorHormiga);

        caminos = new double[numeroDeCiudades][numeroDeCiudades];
        probabilidades = new double[numeroDeCiudades];
        IntStream.range(0, numeroDeHormigas)
            .forEach(i -> hormigas.add(new Hormiga(numeroDeCiudades)));
    }

    /**
     * Generate initial solution
     */
    private int[][] inicializarMatriz() {
        int[][] matrizEEUU = new int[8][8];
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                matrizEEUU[i][j] = 99999999;
            }
        }
        //NY - CHI
        matrizEEUU[0][1] = 1000;
        matrizEEUU[1][0] = 1000;
        //NY - TOR
        matrizEEUU[0][2] = 800;
        matrizEEUU[2][0] = 800;
        //NY - DEN
        matrizEEUU[0][3] = 1900;
        matrizEEUU[3][0] = 1900;
        //NY - HOU
        matrizEEUU[0][7] = 1500;
        matrizEEUU[7][0] = 1500;
        //CHI - TOR
        matrizEEUU[1][2] = 500;
        matrizEEUU[2][1] = 500;
        //CHI - DEN
        matrizEEUU[1][3] = 1000;
        matrizEEUU[3][1] = 1000;
        //TOR - CAL
        matrizEEUU[2][4] = 1500;
        matrizEEUU[4][2] = 1500;
        //TOR - LA
        matrizEEUU[2][5] = 1800;
        matrizEEUU[5][2] = 1800;
        //DEN - UR
        matrizEEUU[3][6] = 1000;
        matrizEEUU[6][3] = 1000;
        //DEN - UR
        matrizEEUU[3][7] = 1500;
        matrizEEUU[7][3] = 1500;
        //Comprobacion
        imprimirMatriz(matrizEEUU);
        return matrizEEUU;
    }

    private void imprimirMatriz(int[][] A){
    System.out.println("valores introducidos:");
        for (int i = 0; i < A.length; i++) { 
            for (int j = 0; j < A[i].length; j++) {
                System.out.print(A[i][j] + " ");
            }
            System.out.println();
        }
    }
    private void imprimirMatriz(double[][] A){
    System.out.println("valores introducidos:");
        for (int i = 0; i < A.length; i++) { 
            for (int j = 0; j < A[i].length; j++) {
                System.out.print(A[i][j] + " ");
            }
            System.out.println();
        }
    }

    public double[][] generateRandomMatrix(int n) {
        double[][] randomMatrix = new double[n][n];
        IntStream.range(0, n)
            .forEach(i -> IntStream.range(0, n)
                .forEach(j -> randomMatrix[i][j] = Math.abs(random.nextInt(100) + 1)));
        imprimirMatriz(randomMatrix);
        return randomMatrix;
    }

    /**
     * Perform ant optimization
     */
    public void startAntOptimization() {
        IntStream.rangeClosed(1, 3)
            .forEach(i -> {
                System.out.println("Simulacion #" + i);
                ejecutar();
            });
    }

    /**
     * Use this method to run the main logic
     */
    public int[] ejecutar() {
        prepararHormigas();
        clearRutas();
        IntStream.range(0, maxIterations)
            .forEach(i -> {
                moverHormigas();
                actualizarRutas();
                actualizarMejor();
            });
        System.out.println("Mejor distancia de recorrido: " + (mejorDistanciaDeRecorrido - numeroDeCiudades));
        System.out.println("Mejor orden para recorrer: " + Arrays.toString(mejorOrdenAlRecorrer));
        return mejorOrdenAlRecorrer.clone();
    }

    /**
     * Prepare hormigas for the simulation
     */
    private void prepararHormigas() {
        IntStream.range(0, numeroDeHormigas)
            .forEach(i -> {
                hormigas.forEach(ant -> {
                    ant.clear();
                    ant.visitaCiudad(-1, random.nextInt(numeroDeCiudades));
                });
            });
        currentIndex = 0;
    }

    /**
     * Mueve a las hormigas en cada iteracion
     */
    private void moverHormigas() {
        IntStream.range(currentIndex, numeroDeCiudades - 1)
            .forEach(i -> {
                hormigas.forEach(ant -> ant.visitaCiudad(currentIndex, seleccionarSiguienteCiudad(ant)));
                currentIndex++;
            });
    }

    /**
     * Selecciona la siguiente ciudad
     */
    private int seleccionarSiguienteCiudad(Hormiga ant) {
        int t = random.nextInt(numeroDeCiudades - currentIndex);
        if (random.nextDouble() < factorAleatorio) {
            OptionalInt cityIndex = IntStream.range(0, numeroDeCiudades)
                .filter(i -> i == t && !ant.visitado(i))
                .findFirst();
            if (cityIndex.isPresent()) {
                return cityIndex.getAsInt();
            }
        }
        calculateProbabilities(ant);
        double r = random.nextDouble();
        double total = 0;
        for (int i = 0; i < numeroDeCiudades; i++) {
            total += probabilidades[i];
            if (total >= r) {
                return i;
            }
        }

        throw new RuntimeException("No hay mas ciudades");
    }

    /**
     * Calculate the next city picks probabilites
     */
    public void calculateProbabilities(Hormiga ant) {
        int i = ant.camino[currentIndex];
        double feromona = 0.0;
        for (int l = 0; l < numeroDeCiudades; l++) {
            if (!ant.visitado(l)) {
                feromona += Math.pow(caminos[i][l], alpha) * Math.pow(1.0 / matriz[i][l], beta);
            }
        }
        for (int j = 0; j < numeroDeCiudades; j++) {
            if (ant.visitado(j)) {
                probabilidades[j] = 0.0;
            } else {
                double numerador = Math.pow(caminos[i][j], alpha) * Math.pow(1.0 / matriz[i][j], beta);
                probabilidades[j] = numerador / feromona;
            }
        }
    }

    /**
     * Caminos recorridos
     */
    private void actualizarRutas() {
        for (int i = 0; i < numeroDeCiudades; i++) {
            for (int j = 0; j < numeroDeCiudades; j++) {
                caminos[i][j] *= evaporacion;
            }
        }
        for (Hormiga a : hormigas) {
            double contribucion = Q / a.distanciaDeRecorrido(matriz);
            for (int i = 0; i < numeroDeCiudades - 1; i++) {
                caminos[a.camino[i]][a.camino[i + 1]] += contribucion;
            }
            caminos[a.camino[numeroDeCiudades - 1]][a.camino[0]] += contribucion;
        }
    }

    /**
     * Update the best solution
     */
    private void actualizarMejor() {
        if (mejorOrdenAlRecorrer == null) {
            mejorOrdenAlRecorrer = hormigas.get(0).camino;
            mejorDistanciaDeRecorrido = hormigas.get(0).distanciaDeRecorrido(matriz);
        }
        for (Hormiga a : hormigas) {
            if (a.distanciaDeRecorrido(matriz) < mejorDistanciaDeRecorrido) {
                mejorDistanciaDeRecorrido = a.distanciaDeRecorrido(matriz);
                mejorOrdenAlRecorrer = a.camino.clone();
            }
        }
    }

    /**
     * Clear caminos after simulation
     */
    private void clearRutas() {
        IntStream.range(0, numeroDeCiudades)
            .forEach(i -> {
                IntStream.range(0, numeroDeCiudades)
                    .forEach(j -> caminos[i][j] = c);
            });
    }

    
}