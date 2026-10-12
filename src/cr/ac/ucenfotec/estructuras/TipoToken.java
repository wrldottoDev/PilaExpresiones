package cr.ac.ucenfotec.estructuras;

/**
 * Categorías léxicas que puede tener un token dentro de una expresión aritmética.
 */
public enum TipoToken {
    /** Número literal, por ejemplo 3, 4.5 o -2. */
    NUMERO,
    /** Variable numérica, por ejemplo x, base o total1. */
    VARIABLE,
    /** Operador binario: + - * / ^ */
    OPERADOR,
    /** Paréntesis de apertura "(". */
    PARENTESIS_IZQ,
    /** Paréntesis de cierre ")". */
    PARENTESIS_DER
}
