package com.example.testapplication

sealed class Request(open var endpoint: String = "", open var body: String = ""){
    class Get(override var endpoint: String) : Request(endpoint)
    class Post(override var endpoint: String, override var body: String) : Request(endpoint, body)
    class Error() : Request()
}