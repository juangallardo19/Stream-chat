# Stream-Chat

Chat grupal en tiempo real con RabbitMQ y Apache Kafka, microservicios Java 17 / Spring Boot, PostgreSQL y Docker Compose.

Este README se completa en la fase 10 (diagrama, cómo levantarlo, variables de `.env` y guion de la demostración).

## Pruebas manuales con `curl`

### Tildes y signos en Git Bash de Windows

Si el JSON va escrito directamente en el comando y lleva caracteres como `¡`, `¿`, `ñ` o tildes, Git Bash en Windows puede dañar esos caracteres antes de que lleguen a `curl`. El servicio recibe bytes que no son UTF-8 válido y responde:

```json
{ "codigoHttp": 400, "mensaje": "El cuerpo de la solicitud no es un JSON válido." }
```

No es un error del código: el mismo mensaje enviado desde un archivo UTF-8 responde `202` y llega intacto a RabbitMQ (comprobado en la fase 1). Desde el navegador (`chat.html`) no ocurre.

Para probar a mano con tildes, guarda el cuerpo en un archivo UTF-8 y envíalo con `--data-binary`:

```bash
curl -i -X POST http://localhost/message -H "Content-Type: application/json; charset=utf-8" --data-binary @mensaje.json
```

Ejemplo de `mensaje.json`:

```json
{ "user": "Ana", "group": "GrupoA", "text": "¡Hola a todos! ¿Cómo están?" }
```
