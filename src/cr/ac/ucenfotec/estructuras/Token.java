package cr.ac.ucenfotec.estructuras;

/**
 * Nodo de la pila. Cada nodo representa un token de la expresión aritmética
 * (número literal, variable, operador o paréntesis) y guarda la referencia al
 * nodo que está debajo de él en la pila.
 */
public class Token {

    private final TipoToken tipo;
    private final String valor;
    /** Nodo que se encuentra inmediatamente debajo en la pila. */
    private Token siguiente;

    public Token(TipoToken tipo, String valor) {
        this.tipo = tipo;
        this.valor = valor;
    }

    /** Crea una copia "suelta" del token (sin enlace), para apilarlo de forma segura. */
    public Token copiar() {
        return new Token(tipo, valor);
    }

    public TipoToken getTipo() {
        return tipo;
    }

    public String getValor() {
        return valor;
    }

    public Token getSiguiente() {
        return siguiente;
    }

    void setSiguiente(Token siguiente) {
        this.siguiente = siguiente;
    }

    public boolean esOperando() {
        return tipo == TipoToken.NUMERO || tipo == TipoToken.VARIABLE;
    }

    /** Prioridad del operador: ^ = 3, * / = 2, + - = 1. Los demás tokens devuelven 0. */
    public int getPrecedencia() {
        if (tipo != TipoToken.OPERADOR) {
            return 0;
        }
        switch (valor) {
            case "^":
                return 3;
            case "*":
            case "/":
                return 2;
            default:
                return 1;
        }
    }

    /** La potencia se asocia por la derecha (2^3^2 = 2^(3^2)); el resto por la izquierda. */
    public boolean esAsociativoDerecha() {
        return "^".equals(valor);
    }

    @Override
    public String toString() {
        return valor;
    }
}
