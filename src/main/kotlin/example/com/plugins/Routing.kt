package com.example.plugins

import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.request.*
import kotlinx.coroutines.*
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction

fun Application.configureRouting(dispatcher: CoroutineDispatcher) {
    routing {
        get("/") {
            call.respondText("Hello, World!")
        }

        get("/users") {
            val users = withContext(Dispatchers.IO) {
                transaction {
                    Users.selectAll().map { toUser(it) }
                }
            }
            call.respond(users)
        }

        post("/users") {
            val user = call.receive<User>()
            val id = withContext(Dispatchers.IO) {
                transaction {
                    Users.insert {
                        it[name] = user.name
                        it[email] = user.email
                    } get Users.id
                }
            }
            call.respond(User(id, user.name, user.email))
        }

        post("/users/batch") {
            val users = call.receive<List<User>>()
            val ids = withContext(dispatcher) {
                users.chunked(1000).map { chunk ->
                    async {
                        transaction {
                            Users.batchInsert(chunk) { user ->
                                this[Users.name] = user.name
                                this[Users.email] = user.email
                            }.map { it[Users.id] }
                        }
                    }
                }.awaitAll().flatten()
            }
            call.respond(ids)
        }
    }
}

data class User(val id: Int = 0, val name: String, val email: String)

fun toUser(row: ResultRow): User =
    User(
        id = row[Users.id],
        name = row[Users.name],
        email = row[Users.email]
    )