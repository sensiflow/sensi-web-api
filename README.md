# Sensiflow API

[![Tests Passing](https://github.com/sensiflow/sensi-web-api/workflows/Java%20CI%20with%20Gradle/badge.svg)](https://github.com/sensiflow/sensi-web-api/actions/workflows/check.yml)

This is the repository housing the codebase for sensiflow's web api.

## Useful Links

- [Documentation](https://docs.sensiflow.org/api/reference)
- [Project Report](https://github.com/sensiflow/main/blob/main/project-docs/Final_Report_V1.pdf).



### Possible Environment variables

##### General

- `PORT` - the port to run the server on

##### Database

- `JDBC_DATABASE_URL` - the url to the database containing the authentication information

##### SSL

- `SECURE` - whether to use SSL or not
- `KEYSTORE_PATH` - the path to the keystore
- `KEY_STORE_PASSWORD` - the password to the keystore
- `SSL_KEY_PASSWORD` - the password to the key in the keystore

##### RabbitMQ

- `RABBITMQ_HOST` - the host of the rabbitmq server
- `RABBITMQ_PORT` - the port of the rabbitmq server
- `RABBITMQ_USERNAME` - the username to the rabbitmq server
- `RABBITMQ_PASSWORD` - the password to the rabbitmq server
- `RABBITMQ_QUEUE` - the default queue to listen to
- `INSTANCE_CTL_QUEUE` - the queue to send instance control messages to
- `INSTANCE_ACK_DEVICE_STATE_QUEUE` - the queue to send instance ack device state messages to
- `INSTANCE_ACK_SCHEDULER_NOTIFICATION_QUEUE` - the queue to send instance ack scheduler notification messages to

### HTTPS

To use SSL, a keystore must be provided by providing its path and its password using the Environment variables above and the variable SECURE must be set to true
Keep in mind that the keystore type must be PKCS12.
The server port will be the same as the Environment variable PORT if it is set, otherwise it will be 8090.
