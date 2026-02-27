# Modulo 3 - Optimizacion SQL

Consulta base:
```sql
SELECT o.order_id, o.order_date, c.customer_name, o.total_amount
FROM orders o
JOIN customers c ON o.customer_id = c.customer_id
WHERE c.country = 'Mexico';
```

## Estrategias de optimizacion
1. Indices correctos
- `customers(country, customer_id)` para filtrar pais y resolver join key sin full scan.
- `orders(customer_id, order_date)` para acelerar join y lecturas por cliente/fecha.

2. Reducir cardinalidad antes del join
- Filtrar primero customers de Mexico (CTE/subquery) y luego unir con orders.
- Beneficio: menor set intermedio en planes con hash/merge join.

3. Estadisticas y mantenimiento
- Actualizar estadisticas (ANALYZE) y reorganizar tablas/indices para evitar planes suboptimos.
- Revisar bloat y fragmentacion por alto volumen de escrituras.

4. Particionamiento de orders
- Particionar por `order_date` (mensual/trimestral) para pruning si existen filtros temporales.
- Reduce I/O en tablas de 10M+ registros.

5. Materialized view para analitica frecuente
- Vista materializada de pedidos por pais refrescada periodicamente.
- Ideal cuando lectura es mucho mayor que escritura.
