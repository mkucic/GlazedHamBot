package uno.rebellious.twitchbot

import com.gikk.twirk.Twirk
import com.gikk.twirk.TwirkBuilder
import com.gikk.twirk.events.TwirkListener
import io.reactivex.Observable
import io.reactivex.disposables.Disposable
import io.reactivex.rxkotlin.toObservable
import io.reactivex.subjects.BehaviorSubject
import uno.rebellious.twitchbot.command.CommandManager
import uno.rebellious.twitchbot.database.Channel
import uno.rebellious.twitchbot.database.DatabaseDAO
import uno.rebellious.twitchbot.model.Settings
import java.util.*

object BotManager {

    private val scanner: Observable<String> = Scanner(System.`in`).useDelimiter("\n").toObservable().share()
    private val SETTINGS = Settings()
    val lastFMUrl =
        "http://ws.audioscrobbler.com/2.0/?method=user.getrecenttracks&user=${SETTINGS.lastFMUser}&api_key=${SETTINGS.lastFMAPI}&format=json&limit=1"
    val spotifyUrl = "https://api.spotify.com/v1/me/player"
    private val basicAuth = Base64.getUrlEncoder().encodeToString("${SETTINGS.clientId}:${SETTINGS.clientSecret}".toByteArray())
    val spotifyBasicAuth = "Authorization" to "Basic $basicAuth"
    private var threadList = HashMap<String, Pair<Thread, Disposable?>>()


    val database = DatabaseDAO()

    fun startTwirkForChannel(channel: Channel) {
        var disposable: Disposable? = null
        val twirkThread = Thread(Runnable {
            val shouldStop = BehaviorSubject.create<Boolean>()
            shouldStop.onNext(false)
            val nick = if (channel.nick.isBlank()) SETTINGS.nick else channel.nick
            val password = if (channel.token.isBlank()) SETTINGS.password else channel.token

            val twirk = TwirkBuilder("#${channel.channel}", nick, password)
                    .setVerboseMode(true)
                    .build()
            twirk.connect()
            twirk.addIrcListener(CommandManager(twirk, channel))
            twirk.addIrcListener(getOnDisconnectListener(twirk, channel))

            disposable = scanner
                    .takeUntil { it == ".quit\n" }
                    .subscribe {
                        if (it == ".quit") {
                            println("Quitting $channel")
                            twirk.close()
                        } else {
                            twirk.channelMessage(it)
                        }
                    }
        })
        twirkThread.name = channel.channel
        twirkThread.start()
        threadList[channel.channel] = Pair(twirkThread, disposable)
    }

    fun stopTwirkForChannel(channel: String) {
        val thread = threadList[channel]
        thread?.first?.interrupt()
        thread?.second?.dispose()
    }

    private fun getOnDisconnectListener(twirk: Twirk, channel: Channel): TwirkListener? {
        return UnoBotBase(twirk, channel)
    }
}