package cr.ac.ucenfotec.estructuras;

/**
 * Pila (LIFO) implementada como lista enlazada simple de nodos {@link Token}.
 * Todas las operaciones fundamentales (apilar, desapilar, cima) son O(1).
 */
public class Pila {

    /** Nodo que está en la cima de la pila. */
    private Token cima;
    private int tamanio;

    /**
     * push: coloca una copia del token en la cima.
     * Se apila una copia para que un mismo token nunca quede enlazado en dos pilas.
     */
    public void apilar(Token token) {
        Token nuevo = token.copiar();
        nuevo.setSiguiente(cima);
        cima = nuevo;
        tamanio++;
    }

    /** pop: retira y devuelve el token de la cima. */
    public Token desapilar() {
        if (estaVacia()) {
            throw new IllegalStateException("Subdesbordamiento: la pila está vacía.");
        }
        Token retirado = cima;
        cima = cima.getSiguiente();
        retirado.setSiguiente(null);
        tamanio--;
        return retirado;
    }

    /** peek: devuelve el token de la cima sin retirarlo. */
    public Token verCima() {
        if (estaVacia()) {
            throw new IllegalStateException("La pila está vacía, no hay cima.");
        }
        return cima;
    }

    public boolean estaVacia() {
        return cima == null;
    }

    public int getTamanio() {
        return tamanio;
    }

    public void vaciar() {
        cima = null;
        tamanio = 0;
    }

    /** Representación en una línea, de la base (izquierda) a la cima (derecha). */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (Token actual = cima; actual != null; actual = actual.getSiguiente()) {
            sb.insert(0, actual.getValor() + (sb.length() > 0 ? " " : ""));
        }
        return "[" + sb + "]";
    }

    /** Dibujo vertical de la pila, con la cima arriba. */
    public String dibujar() {
        if (estaVacia()) {
            return "  (pila vacía)\n";
        }
        StringBuilder sb = new StringBuilder();
        boolean primero = true;
        for (Token actual = cima; actual != null; actual = actual.getSiguiente()) {
            sb.append(String.format("  | %-10s | %-14s |%s%n",
                    actual.getValor(), actual.getTipo(), primero ? "  <- cima" : ""));
            primero = false;
        }
        sb.append("  +------------+----------------+\n");
        return sb.toString();
    }
}
