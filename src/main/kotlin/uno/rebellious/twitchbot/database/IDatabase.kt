package uno.rebellious.twitchbot.database

import uno.rebellious.twitchbot.model.SpotifyToken
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.*

interface IDatabase : ICounters, IQuotes, ISettings, IResponse, ISpotify

interface ISpotify {
    fun setTokensForChannel(channel: String, accessToken: String, refreshToken: String, expiryTime: LocalDateTime)
    fun getTokensForChannel(channel: String): SpotifyToken?
}

interface IResponse {
    fun findResponse(channel: String, command: String): String
    fun setResponse(channel: String, command: String, response: String)
    fun removeResponse(channel: String, command: String)
}

interface ISettings {
    fun getAllCommandList(channel: String): ArrayList<String>
    fun leaveChannel(channel: String)
    fun addChannel(newChannel: String, prefix: String = "!")
    fun getPrefixForChannel(channel: String): String
    fun setPrefixForChannel(channel: String, prefix: String)
    fun getListOfChannels(): Array<Channel>
}

interface ICounters {
    fun createCounterForChannel(channel: String, counter: String, responseSingular: String, responsePlural: String)
    fun removeCounterForChannel(channel: String, counter: String)
    fun incrementCounterForChannel(channel: String, counter: String, by: Int = 1)
    fun getCounterForChannel(channel: String, counter: String): String
    fun resetTodaysCounterForChannel(channel: String, counter: String)
    fun showCountersForChannel(channel: String): List<String>
}

interface IQuotes {
    fun addQuoteForChannel(channel: String, date: LocalDate, person: String, quote: String): Int
    fun delQuoteForChannel(channel: String, quoteId: Int)
    fun editQuoteForChannel(channel: String, quoteId: Int, date: LocalDate?, person: String, quote: String)
    fun getQuoteForChannelById(channel: String, quoteId: Int): String
    fun getRandomQuoteForChannel(channel: String): String
    fun findQuoteByAuthor(channel: String, author: String): String
    fun findQuoteByKeyword(channel: String, keyword: String): String
    fun undeleteQuoteForChannel(channel: String, quoteId: Int)
}