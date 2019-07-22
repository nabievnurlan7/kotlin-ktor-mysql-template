package com.anychart

import com.google.gson.Gson
import kotlinx.html.*
import kotlinx.html.stream.appendHTML
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.ktor.netty.*
import org.jetbrains.ktor.routing.*
import org.jetbrains.ktor.host.*
import org.jetbrains.ktor.http.*
import org.jetbrains.ktor.response.*

object Questions : Table("questions") {
    val id = integer("id").primaryKey()
    val questionKz = varchar("question_kz", length = 256)
    val questionRu = varchar("question_ru", length = 256)
    val description = varchar("description", length = 256)

}

data class Question(
        val id: Int,
        val questionKz: String,
        val questionRu: String,
        val description : String)

/*
    Init MySQL database connection
 */
fun initDB() {
    val url = "jdbc:mysql://root:2323N**lan@localhost:3306/hyzmet_db?useUnicode=true&serverTimezone=UTC"
    val driver = "com.mysql.cj.jdbc.Driver"
    Database.connect(url, driver)
}

/*
    Getting fruit data from database
 */
fun getQuestions(): String {
    var json = ""
    transaction {
        val result = Questions.selectAll().orderBy(Questions.id, false).limit(5)
        val arrayList = ArrayList<Question>()
        for (question in result) {
            arrayList.add(
                    Question(
                            id = question[Questions.id],
                            questionKz = question[Questions.questionKz],
                            questionRu = question[Questions.questionRu],
                            description = question[Questions.description])
            )
        }
        json = Gson().toJson(arrayList)
    }
    return json
}

/*
    Main function
 */
fun main(args: Array<String>) {
    initDB()
    embeddedServer(Netty, 8080) {
        routing {
            get("/") {
                call.respondText(getQuestions(), ContentType.Text.Html)
            }

            get("/login") {
                call.respondText(template(getQuestions()), ContentType.Text.Html)
            }

            get("/questions") {
                call.respondText(template(getQuestions()), ContentType.Text.Html)
            }

            get("/result") {
                call.respondText(template(getQuestions()), ContentType.Text.Html)
            }
        }
    }.start(wait = true)
}

/*
    HTML template
 */
fun template(fruitsJson: String): String {
    return StringBuilder().appendHTML().html {
        lang = "en"
        head {
            meta { charset = "UTF-8" }
            title { +"AnyChart Kotlin Ktor MySQL template" }
            script { src = "https://cdn.anychart.com/releases/v8/js/anychart-base.min.js" }
            script { src = "https://cdn.anychart.com/releases/v8/js/anychart-exports.min.js" }
            script { src = "https://cdn.anychart.com/releases/v8/js/anychart-vml.min.js" }
            link {
                href = "https://cdn.anychart.com/releases/v8/css/anychart-ui.min.css"
                rel = "stylesheet"
            }
            link {
                href = "https://cdn.anychart.com/releases/v8/fonts/css/anychart.min.css"
                rel = "stylesheet"
            }
            style {
                +"""html, body, #container {
                        width: 400px;
                        height: 400px;
                        margin: 0;
                        padding: 0;
                    }""".trimIndent()
            }
        }
        body {
            div { id = "container" }
            script {
                unsafe {
                    +"""var chart = anychart.pie($fruitsJson);
                    chart.title('Top 5 fruits');
                    chart.container('container');
                    chart.draw();
                """
                }
            }
        }
    }.toString()
}