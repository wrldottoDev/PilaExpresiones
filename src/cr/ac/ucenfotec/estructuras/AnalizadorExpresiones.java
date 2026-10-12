package cr.ac.ucenfotec.estructuras;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Análisis de expresiones aritméticas apoyado en la clase {@link Pila}:
 * tokenización, verificación de paréntesis, conversión infija a postfija
 * (algoritmo shunting-yard de Dijkstra) y evaluación de la expresión postfija.
 * Cada algoritmo puede registrar paso a paso el estado de la pila en una traza.
 */
public class AnalizadorExpresiones {

    private static final String OPERADORES = "+-*/^";

    /**
     * Divide el texto en tokens. Reconoce números (enteros o decimales),
     * variables (letra seguida de letras, dígitos o _), operadores y paréntesis.
     * Un "-" en posición de operando (al inicio, tras "(" o tras otro operador)
     * se interpreta como signo de un número negativo.
     */
    public List<Token> tokenizar(String expresion) {
        List<Token> tokens = new ArrayList<>();
        int i = 0;
        while (i < expresion.length()) {
            char c = expresion.charAt(i);
            if (Character.isWhitespace(c)) {
                i++;
            } else if (Character.isDigit(c) || c == '.' || (c == '-' && esperaOperando(tokens)
                    && i + 1 < expresion.length() && (Character.isDigit(expresion.charAt(i + 1))
                    || expresion.charAt(i + 1) == '.'))) {
                int inicio = i++;
                while (i < expresion.length()
                        && (Character.isDigit(expresion.charAt(i)) || expresion.charAt(i) == '.')) {
                    i++;
                }
                String numero = expresion.substring(inicio, i);
                validarNumero(numero, inicio);
                tokens.add(new Token(TipoToken.NUMERO, numero));
            } else if (Character.isLetter(c)) {
                int inicio = i;
                while (i < expresion.length()
                        && (Character.isLetterOrDigit(expresion.charAt(i)) || expresion.charAt(i) == '_')) {
                    i++;
                }
                tokens.add(new Token(TipoToken.VARIABLE, expresion.substring(inicio, i)));
            } else if (OPERADORES.indexOf(c) >= 0) {
                tokens.add(new Token(TipoToken.OPERADOR, String.valueOf(c)));
                i++;
            } else if (c == '(') {
                tokens.add(new Token(TipoToken.PARENTESIS_IZQ, "("));
                i++;
            } else if (c == ')') {
                tokens.add(new Token(TipoToken.PARENTESIS_DER, ")"));
                i++;
            } else {
                throw new IllegalArgumentException(
                        "Carácter no válido '" + c + "' en la posición " + (i + 1) + ".");
            }
        }
        if (tokens.isEmpty()) {
            throw new IllegalArgumentException("La expresión está vacía.");
        }
        return tokens;
    }

    /**
     * Verifica que los paréntesis estén balanceados usando la pila:
     * cada "(" se apila y cada ")" desapila su pareja.
     */
    public boolean parentesisBalanceados(List<Token> tokens, List<String> traza) {
        Pila pila = new Pila();
        for (Token t : tokens) {
            if (t.getTipo() == TipoToken.PARENTESIS_IZQ) {
                pila.apilar(t);
                registrar(traza, "push '('", pila);
            } else if (t.getTipo() == TipoToken.PARENTESIS_DER) {
                if (pila.estaVacia()) {
                    registrar(traza, "')' sin pareja: la pila está vacía", pila);
                    return false;
                }
                pila.desapilar();
                registrar(traza, "pop  '(' (empareja con ')')", pila);
            }
        }
        if (!pila.estaVacia()) {
            registrar(traza, "Quedan " + pila.getTamanio() + " '(' sin cerrar", pila);
        }
        return pila.estaVacia();
    }

    /**
     * Convierte la expresión infija a notación postfija (polaca inversa) con el
     * algoritmo shunting-yard. La pila guarda los operadores y paréntesis pendientes.
     */
    public List<Token> aPostfija(List<Token> tokens, List<String> traza) {
        validarSintaxis(tokens);
        List<Token> salida = new ArrayList<>();
        Pila operadores = new Pila();

        for (Token t : tokens) {
            switch (t.getTipo()) {
                case NUMERO:
                case VARIABLE:
                    // Los operandos pasan directo a la salida.
                    salida.add(t);
                    registrar(traza, "'" + t + "' a la salida", operadores, salida);
                    break;
                case OPERADOR:
                    // Sacar los operadores de mayor (o igual, si es asociativo izq.) prioridad.
                    while (!operadores.estaVacia()
                            && operadores.verCima().getTipo() == TipoToken.OPERADOR
                            && debeSalirAntes(operadores.verCima(), t)) {
                        salida.add(operadores.desapilar());
                        registrar(traza, "pop '" + salida.get(salida.size() - 1) + "' a la salida",
                                operadores, salida);
                    }
                    operadores.apilar(t);
                    registrar(traza, "push '" + t + "'", operadores, salida);
                    break;
                case PARENTESIS_IZQ:
                    operadores.apilar(t);
                    registrar(traza, "push '('", operadores, salida);
                    break;
                case PARENTESIS_DER:
                    // Vaciar hasta encontrar el "(" correspondiente.
                    while (!operadores.estaVacia()
                            && operadores.verCima().getTipo() != TipoToken.PARENTESIS_IZQ) {
                        salida.add(operadores.desapilar());
                        registrar(traza, "pop '" + salida.get(salida.size() - 1) + "' a la salida",
                                operadores, salida);
                    }
                    if (operadores.estaVacia()) {
                        throw new IllegalArgumentException("Paréntesis ')' sin su '(' correspondiente.");
                    }
                    operadores.desapilar();
                    registrar(traza, "pop '(' y se descarta", operadores, salida);
                    break;
                default:
                    break;
            }
        }
        while (!operadores.estaVacia()) {
            Token t = operadores.desapilar();
            if (t.getTipo() == TipoToken.PARENTESIS_IZQ) {
                throw new IllegalArgumentException("Paréntesis '(' sin cerrar.");
            }
            salida.add(t);
            registrar(traza, "pop '" + t + "' a la salida (fin)", operadores, salida);
        }
        return salida;
    }

    /**
     * Evalúa una expresión postfija. La pila guarda los operandos (como tokens
     * numéricos); al llegar un operador se desapilan dos, se opera y se apila el resultado.
     */
    public double evaluarPostfija(List<Token> postfija, Map<String, Double> variables, List<String> traza) {
        Pila operandos = new Pila();
        for (Token t : postfija) {
            if (t.getTipo() == TipoToken.NUMERO) {
                operandos.apilar(t);
                registrar(traza, "push " + t, operandos);
            } else if (t.getTipo() == TipoToken.VARIABLE) {
                Double valor = variables.get(t.getValor());
                if (valor == null) {
                    throw new IllegalArgumentException("La variable '" + t + "' no tiene un valor asignado.");
                }
                operandos.apilar(new Token(TipoToken.NUMERO, formatear(valor)));
                registrar(traza, "push " + t + " = " + formatear(valor), operandos);
            } else {
                if (operandos.getTamanio() < 2) {
                    throw new IllegalArgumentException("Faltan operandos para el operador '" + t + "'.");
                }
                // El orden importa: el primero en salir es el operando derecho.
                double derecho = Double.parseDouble(operandos.desapilar().getValor());
                double izquierdo = Double.parseDouble(operandos.desapilar().getValor());
                double resultado = operar(izquierdo, derecho, t.getValor());
                operandos.apilar(new Token(TipoToken.NUMERO, formatear(resultado)));
                registrar(traza, "pop " + formatear(derecho) + ", pop " + formatear(izquierdo) + ", "
                        + formatear(izquierdo) + " " + t + " " + formatear(derecho) + " = "
                        + formatear(resultado) + ", push", operandos);
            }
        }
        if (operandos.getTamanio() != 1) {
            throw new IllegalArgumentException("Expresión mal formada: sobran operandos.");
        }
        return Double.parseDouble(operandos.desapilar().getValor());
    }

    /** Escribe un número sin ".0" cuando es entero. */
    public static String formatear(double valor) {
        if (valor == Math.rint(valor) && !Double.isInfinite(valor) && Math.abs(valor) < 1e15) {
            return String.valueOf((long) valor);
        }
        return String.valueOf(valor);
    }

    // ---------------------------------------------------------------- auxiliares

    private boolean esperaOperando(List<Token> tokens) {
        if (tokens.isEmpty()) {
            return true;
        }
        TipoToken ultimo = tokens.get(tokens.size() - 1).getTipo();
        return ultimo == TipoToken.OPERADOR || ultimo == TipoToken.PARENTESIS_IZQ;
    }

    private void validarNumero(String numero, int posicion) {
        try {
            Double.parseDouble(numero);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(
                    "Número mal formado '" + numero + "' en la posición " + (posicion + 1) + ".");
        }
    }

    /** Revisa que operandos y operadores alternen correctamente (p. ej. rechaza "3 + * 4" o "3 4"). */
    private void validarSintaxis(List<Token> tokens) {
        boolean esperaOperando = true;
        for (Token t : tokens) {
            if (t.esOperando() || t.getTipo() == TipoToken.PARENTESIS_IZQ) {
                if (!esperaOperando) {
                    throw new IllegalArgumentException("Falta un operador antes de '" + t + "'.");
                }
                esperaOperando = t.getTipo() == TipoToken.PARENTESIS_IZQ;
            } else {
                if (esperaOperando) {
                    throw new IllegalArgumentException("Falta un operando antes de '" + t + "'.");
                }
                esperaOperando = t.getTipo() == TipoToken.OPERADOR;
            }
        }
        if (esperaOperando) {
            throw new IllegalArgumentException("La expresión termina sin un operando.");
        }
    }

    private boolean debeSalirAntes(Token enCima, Token entrante) {
        return enCima.getPrecedencia() > entrante.getPrecedencia()
                || (enCima.getPrecedencia() == entrante.getPrecedencia() && !entrante.esAsociativoDerecha());
    }

    private double operar(double a, double b, String operador) {
        switch (operador) {
            case "+":
                return a + b;
            case "-":
                return a - b;
            case "*":
                return a * b;
            case "/":
                if (b == 0) {
                    throw new ArithmeticException("División entre cero.");
                }
                return a / b;
            case "^":
                return Math.pow(a, b);
            default:
                throw new IllegalArgumentException("Operador desconocido: " + operador);
        }
    }

    private void registrar(List<String> traza, String accion, Pila pila) {
        if (traza != null) {
            traza.add(String.format("%-48s pila: %s", accion, pila));
        }
    }

    private void registrar(List<String> traza, String accion, Pila pila, List<Token> salida) {
        if (traza != null) {
            traza.add(String.format("%-30s pila: %-16s salida: %s", accion, pila, unir(salida)));
        }
    }

    public static String unir(List<Token> tokens) {
        StringBuilder sb = new StringBuilder();
        for (Token t : tokens) {
            sb.append(sb.length() > 0 ? " " : "").append(t.getValor());
        }
        return sb.toString();
    }
}
