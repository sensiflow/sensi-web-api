package com.isel.sensiflow.amqp.message

import com.isel.sensiflow.amqp.message.input.InstanceCtlAcknowledgeMessage

interface MessageHandler {

    /**
     * Handles the received message.
     */
    fun handle(message: InstanceCtlAcknowledgeMessage)
}
