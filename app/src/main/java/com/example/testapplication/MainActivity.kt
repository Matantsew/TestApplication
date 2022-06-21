package com.example.testapplication

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.testapplication.databinding.ActivityMainBinding
import java.io.*
import java.net.ServerSocket
import java.net.Socket

class MainActivity : AppCompatActivity() {

    companion object {
        const val SocketServerPORT = 8080
    }

    private lateinit var viewBinding: ActivityMainBinding

    private lateinit var serverSocket: ServerSocket

    private var message = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewBinding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(viewBinding.root)

        launchServer()
    }

    private fun launchServer(){

        val socketServerThread = Thread {

            var count = 0

            try {

                serverSocket = ServerSocket(SocketServerPORT)

                runOnUiThread {
                    val text = "I'm waiting here: " + serverSocket.localPort
                    viewBinding.infoTextView.text = text
                }

                while(true) {

                    val socket: Socket = serverSocket.accept()

                    val inputStream = socket.getInputStream()
                    val inputStreamReader = InputStreamReader(inputStream)
                    val bReader = BufferedReader(inputStreamReader)

                    val request = obtainRequest(bReader)

                    count++
                    message = "#$count from ${socket.inetAddress}:${socket.port}\n"

                    runOnUiThread {
                        viewBinding.messageTextView.text = message
                    }

                    val socketServerReplyThread = Thread {

                        val outputStream: OutputStream
                        val msgReply = "Hello from Android, you are #$count\n"

                        try {

                            outputStream = socket.getOutputStream()

                            val pw = PrintWriter(outputStream, true)
                            val response = "{\n" +
                                    "  \"status\": \"OK\",\n" +
                                    "  \"data\": [\n" +
                                    "    \"sale\",\n" +
                                    "    \"receipt\"\n" +
                                    "  ]\n" +
                                    "}\n"

                            pw.print(
                                """
                    HTTP/1.0 200
                    
                    """.trimIndent())

                            pw.print(
                                """
                    Content type: application/json
                    
                    """.trimIndent())

                            pw.print(
                                """
                    Content length: ${response.length}
                    
                    """.trimIndent())

                            pw.print("\r\n")
                            pw.print(
                                """
                    $response
                    
                    """.trimIndent())

                            pw.flush()

                            socket.close()
                            inputStream.close()
                            inputStreamReader.close()
                            bReader.close()

                            message = "replayed: $msgReply\n"

                            runOnUiThread {
                                viewBinding.messageTextView.text = message
                            }

                        } catch (e: IOException) {
                            e.printStackTrace()
                            message = "Something wrong! $e\n"
                        }

                        runOnUiThread {
                            viewBinding.messageTextView.text = message
                        }
                    }

                    socketServerReplyThread.run()
                }
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }

        socketServerThread.start()
    }

    private fun obtainRequest(bReader: BufferedReader) : Request{

        fun Reader.copyToSingle(out: Writer, bufferSize: Int = DEFAULT_BUFFER_SIZE): Long {
            val charsCopied: Long = 0
            val buffer = CharArray(bufferSize)
            val chars = read(buffer)

            out.write(buffer, 0, chars)
            return charsCopied
        }

        fun Reader.readTextSingle(): String {
            val buffer = StringWriter()
            copyToSingle(buffer)
            return buffer.toString()
        }

        fun checkEndpoint(line: String): Boolean {
            return with(line){
                equals("supported_operations") ||
                equals("sale") ||
                equals("receipt")
            }
        }

        var inputLine = bReader.readLine()

        val afterMethodIndex = inputLine.indexOf("/")
        val command = inputLine.substring(0, afterMethodIndex).trim()

        var raw = bReader.readTextSingle()

        inputLine = inputLine.substring(afterMethodIndex + 1)
        inputLine = inputLine.substring(0, inputLine.indexOf(' '))

        val endpoint = if(checkEndpoint(inputLine)) inputLine else ""

        return when(command){
            "GET" -> {
                Request.Get(endpoint)
            }
            "POST" -> {
                raw = raw.substring(raw.indexOf("{"))
                Request.Post(endpoint, raw)
            }
            else -> Request.Error()         // Additional
        }
    }

    override fun onDestroy() {
        super.onDestroy()

        try {
            serverSocket.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }
}