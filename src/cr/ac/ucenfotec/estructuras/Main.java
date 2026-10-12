package cr.ac.ucenfotec.estructuras;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

/**
 * Punto de entrada del programa. Presenta un menú de consola para analizar
 * expresiones aritméticas con una pila y para operar la pila manualmente.
 */
public class Main {

    private static final Scanner ENTRADA = new Scanner(System.in);
    private static final AnalizadorExpresiones ANALIZADOR = new AnalizadorExpresiones();

    /** Expresión con la que se trabaja actualmente (ya tokenizada). */
    private static String expresion;
    private static List<Token> tokens;
    /** Valores asignados a las variables de la expresión. */
    private static final Map<String, Double> VARIABLES = new LinkedHashMap<>();
    /** Pila que el usuario manipula directamente desde el submenú. */
    private static final Pila PILA_MANUAL = new Pila();

    public static void main(String[] args) {
        System.out.println("==============================================");
        System.out.println("   ANALIZADOR DE EXPRESIONES ARITMÉTICAS");
        System.out.println("   con una pila implementada en Java");
        System.out.println("==============================================");

        boolean continuar = true;
        while (continuar) {
            continuar = menu();
        }
        System.out.println("¡Hasta luego!");
    }

    /**
     * Muestra el menú principal, ejecuta la opción elegida y devuelve
     * false cuando el usuario decide salir.
     */
    private static boolean menu() {
        System.out.println();
        System.out.println("Expresión actual: " + (expresion == null ? "(ninguna)" : expresion));
        System.out.println(" 1. Ingresar una expresión");
        System.out.println(" 2. Ver los tokens de la expresión");
        System.out.println(" 3. Verificar paréntesis balanceados");
        System.out.println(" 4. Convertir a notación postfija (paso a paso)");
        System.out.println(" 5. Asignar valores a las variables");
        System.out.println(" 6. Evaluar la expresión (paso a paso)");
        System.out.println(" 7. Operar la pila manualmente (push / pop / peek)");
        System.out.println(" 0. Salir");

        String opcion = leer("Elija una opción: ");
        try {
            switch (opcion) {
                case "1":
                    ingresarExpresion();
                    break;
                case "2":
                    if (hayExpresion()) {
                        mostrarTokens();
                    }
                    break;
                case "3":
                    if (hayExpresion()) {
                        verificarParentesis();
                    }
                    break;
                case "4":
                    if (hayExpresion()) {
                        convertirPostfija();
                    }
                    break;
                case "5":
                    if (hayExpresion()) {
                        asignarVariables(true);
                    }
                    break;
                case "6":
                    if (hayExpresion()) {
                        evaluar();
                    }
                    break;
                case "7":
                    menuPilaManual();
                    break;
                case "0":
                    return false;
                default:
                    System.out.println("Opción no válida, intente de nuevo.");
            }
        } catch (IllegalArgumentException | IllegalStateException | ArithmeticException e) {
            System.out.println("Error: " + e.getMessage());
        }
        return true;
    }

    // ------------------------------------------------------------ opciones

    private static void ingresarExpresion() {
        System.out.println("Ejemplos: (a + 3) * b - 4 / 2      2 ^ 3 ^ 2      (x - 1.5) * (y + 2)");
        String texto = leer("Expresión: ");
        tokens = ANALIZADOR.tokenizar(texto);
        expresion = texto;
        VARIABLES.keySet().retainAll(nombresVariables());
        System.out.println("Expresión registrada con " + tokens.size() + " tokens.");
    }

    private static void mostrarTokens() {
        System.out.printf("  %-4s %-10s %s%n", "#", "Valor", "Tipo");
        for (int i = 0; i < tokens.size(); i++) {
            System.out.printf("  %-4d %-10s %s%n", i + 1, tokens.get(i).getValor(), tokens.get(i).getTipo());
        }
    }

    private static void verificarParentesis() {
        List<String> traza = new ArrayList<>();
        boolean ok = ANALIZADOR.parentesisBalanceados(tokens, traza);
        imprimirTraza(traza);
        System.out.println(ok ? "Los paréntesis están balanceados." : "Los paréntesis NO están balanceados.");
    }

    private static void convertirPostfija() {
        List<String> traza = new ArrayList<>();
        List<Token> postfija = ANALIZADOR.aPostfija(tokens, traza);
        imprimirTraza(traza);
        System.out.println("Postfija: " + AnalizadorExpresiones.unir(postfija));
    }

    private static void evaluar() {
        List<Token> postfija = ANALIZADOR.aPostfija(tokens, null);
        asignarVariables(false);
        List<String> traza = new ArrayList<>();
        double resultado = ANALIZADOR.evaluarPostfija(postfija, VARIABLES, traza);
        System.out.println("Postfija: " + AnalizadorExpresiones.unir(postfija));
        imprimirTraza(traza);
        System.out.println("Resultado: " + expresion + " = " + AnalizadorExpresiones.formatear(resultado));
    }

    /**
     * Pide el valor de las variables de la expresión. Si {@code todas} es false,
     * solo pregunta por las que todavía no tienen valor.
     */
    private static void asignarVariables(boolean todas) {
        List<String> nombres = nombresVariables();
        if (nombres.isEmpty()) {
            if (todas) {
                System.out.println("La expresión no tiene variables.");
            }
            return;
        }
        for (String nombre : nombres) {
            if (todas || !VARIABLES.containsKey(nombre)) {
                VARIABLES.put(nombre, leerNumero("Valor de " + nombre + ": "));
            }
        }
        System.out.println("Variables: " + VARIABLES);
    }

    /** Submenú para practicar las operaciones fundamentales de la pila. */
    private static void menuPilaManual() {
        while (true) {
            System.out.println();
            System.out.print(PILA_MANUAL.dibujar());
            System.out.println("Tamaño: " + PILA_MANUAL.getTamanio());
            System.out.println(" 1. push (apilar un token)   2. pop (desapilar)   3. peek (ver cima)");
            System.out.println(" 4. vaciar la pila           0. volver al menú principal");
            String opcion = leer("Elija una opción: ");
            try {
                switch (opcion) {
                    case "1":
                        List<Token> nuevos = ANALIZADOR.tokenizar(leer("Token(s) a apilar: "));
                        for (Token t : nuevos) {
                            PILA_MANUAL.apilar(t);
                        }
                        System.out.println("Apilados: " + AnalizadorExpresiones.unir(nuevos));
                        break;
                    case "2":
                        Token t = PILA_MANUAL.desapilar();
                        System.out.println("Desapilado: " + t + " (" + t.getTipo() + ")");
                        break;
                    case "3":
                        Token cima = PILA_MANUAL.verCima();
                        System.out.println("Cima: " + cima + " (" + cima.getTipo() + ")");
                        break;
                    case "4":
                        PILA_MANUAL.vaciar();
                        System.out.println("Pila vaciada.");
                        break;
                    case "0":
                        return;
                    default:
                        System.out.println("Opción no válida.");
                }
            } catch (IllegalArgumentException | IllegalStateException e) {
                System.out.println("Error: " + e.getMessage());
            }
        }
    }

    // ------------------------------------------------------------ utilidades

    private static boolean hayExpresion() {
        if (tokens == null) {
            System.out.println("Primero ingrese una expresión (opción 1).");
            return false;
        }
        return true;
    }

    private static List<String> nombresVariables() {
        List<String> nombres = new ArrayList<>();
        for (Token t : tokens) {
            if (t.getTipo() == TipoToken.VARIABLE && !nombres.contains(t.getValor())) {
                nombres.add(t.getValor());
            }
        }
        return nombres;
    }

    private static void imprimirTraza(List<String> traza) {
        for (int i = 0; i < traza.size(); i++) {
            System.out.printf("  %2d. %s%n", i + 1, traza.get(i));
        }
    }

    private static String leer(String mensaje) {
        System.out.print(mensaje);
        if (!ENTRADA.hasNextLine()) {
            System.out.println();
            System.exit(0);
        }
        return ENTRADA.nextLine().trim();
    }

    private static double leerNumero(String mensaje) {
        while (true) {
            try {
                return Double.parseDouble(leer(mensaje).replace(',', '.'));
            } catch (NumberFormatException e) {
                System.out.println("Debe ingresar un número.");
            }
        }
    }
}
