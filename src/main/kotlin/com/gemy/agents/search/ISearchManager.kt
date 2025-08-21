package com.gemy.agents.search

interface ISearchManager {

    suspend fun search(query: String): String

    suspend fun fetchWebsite(url: String): String
}