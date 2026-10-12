# PilaExpresiones

Primer estudio de caso, SOFT-10 Estructuras de Datos (Universidad CENFOTEC, C3-2026).

Analizador de expresiones aritméticas (números literales, variables, operadores `+ - * / ^` y paréntesis) construido sobre una pila enlazada propia, cuyos nodos son los tokens de la expresión.

## Clases

- `Token`: nodo de la pila (tipo, valor y referencia al siguiente nodo).
- `TipoToken`: categorías de token.
- `Pila`: pila enlazada con push (`apilar`), pop (`desapilar`), peek (`verCima`), `estaVacia`, `getTamanio`, `vaciar`.
- `AnalizadorExpresiones`: tokenización, verificación de paréntesis, conversión infija a postfija (shunting-yard) y evaluación postfija con trazas paso a paso.
- `Main`: rutina `main()` con el `menu()` de consola.

## Ejecución

```bash
javac -d out src/cr/ac/ucenfotec/estructuras/*.java
java -cp out cr.ac.ucenfotec.estructuras.Main
```
