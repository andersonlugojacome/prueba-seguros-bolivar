# Modulo 4 - Git Strategy

Para traer un cambio especifico de `main` a `feature/new-login` sin incorporar todo `main`, usar `cherry-pick`.

## Comandos
```bash
git checkout feature/new-login
git fetch origin
git log origin/main --oneline
# identificar el commit critico, ejemplo abc1234
git cherry-pick abc1234
```

## Por que
- Aplica exactamente un commit puntual.
- Evita mezclar cambios no relacionados de `main`.
- Mantiene trazabilidad del fix de seguridad dentro de la rama feature.

Si el fix depende de otros commits, se cherry-pickean en cadena o se usa `-x` para dejar referencia explicita al hash original.
