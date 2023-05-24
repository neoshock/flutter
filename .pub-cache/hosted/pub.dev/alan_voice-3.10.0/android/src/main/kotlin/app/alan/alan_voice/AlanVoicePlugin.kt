package app.alan.alan_voice

import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.common.MethodChannel.MethodCallHandler
import io.flutter.plugin.common.MethodChannel.Result
import io.flutter.plugin.common.EventChannel.StreamHandler;
import io.flutter.plugin.common.PluginRegistry.Registrar
import io.flutter.plugin.common.EventChannel
import io.flutter.plugin.common.EventChannel.EventSink
import io.flutter.embedding.engine.plugins.activity.ActivityAware
import io.flutter.embedding.engine.plugins.FlutterPlugin
import io.flutter.embedding.engine.plugins.activity.ActivityPluginBinding
import android.widget.FrameLayout
import android.app.Activity
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.view.*
import androidx.annotation.NonNull
import com.alan.alansdk.Alan
import com.alan.alansdk.AlanCallback
import com.alan.alansdk.logging.AlanLogger
import com.alan.alansdk.button.AlanButton
import com.alan.alansdk.events.EventCommand
import com.alan.alansdk.events.EventRecognised
import com.alan.alansdk.events.EventText
import com.alan.alansdk.events.EventParsed
import com.alan.alansdk.qr.BarcodeEvent
import com.alan.alansdk.AlanState
import com.alan.alansdk.AlanConfig
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

class AlanVoicePlugin: FlutterPlugin, MethodCallHandler, StreamHandler, ActivityAware {

  private var applicationContext: Context? = null
  private var activity: Activity? = null

  private var alanButton: AlanButton? = null

  private lateinit var channel : MethodChannel
  private lateinit var callBackChannel: EventChannel

  private val alanSink = AlanEventSink();
  private var sink : EventSink? = null

  private val ARGUMENT_LOG_LEVEL = "logLevel"
  private val ARGUMENT_PROJECT_ID = "projectId"
  private val ARGUMENT_WAKEWORD_ENABLED = "wakeword_enabled"
  private val ARGUMENT_PROJECT_AUTH_JSON = "projectAuthJson"
  private val ARGUMENT_PROJECT_SERVER = "projectServer"
  private val ARGUMENT_PLUGIN_VERSION = "wrapperVersion"

  private val ARGUMENT_BUTTON_HORIZONTAL_ALIGN = "buttonAlign"
  private val ARGUMENT_BUTTON_TOP_MARGIN = "topMargin"
  private val ARGUMENT_BUTTON_BOTTOM_MARGIN = "bottomMargin"
  private val ARGUMENT_STT_VISIBLE = "sttVisible"

  private val ARGUMENT_METHOD_NAME = "method_name"
  private val ARGUMENT_METHOD_ARGS = "method_args"

  private val ARGUMENT_VISUALS = "visuals"
  private val ARGUMENT_TEXT = "text"
  private val ARGUMENT_COMMAND = "command"

  init {
    EventBus.getDefault().register(this)
  }

  companion object {

    @JvmStatic
    fun registerWith(registrar: Registrar) {
      val channel = MethodChannel(registrar.messenger(), "alan_voice")
      channel.setMethodCallHandler(AlanVoicePlugin())

      val callBackChannel = EventChannel(registrar.messenger(), "alan_voice_callback")
      callBackChannel.setStreamHandler(AlanVoicePlugin())
    }
  }

  override fun onDetachedFromActivity() {
    activity = null
  }

  override fun onReattachedToActivityForConfigChanges(binding: ActivityPluginBinding) {
    activity = binding.activity;
  }

  override fun onAttachedToActivity(binding: ActivityPluginBinding) {
    activity = binding.activity;
  }

  override fun onDetachedFromActivityForConfigChanges() {
  }

  override fun onAttachedToEngine(@NonNull flutterPluginBinding: FlutterPlugin.FlutterPluginBinding) {
    channel = MethodChannel(flutterPluginBinding.binaryMessenger, "alan_voice")
    channel.setMethodCallHandler(this)

    callBackChannel = EventChannel(flutterPluginBinding.binaryMessenger, "alan_voice_callback")
    callBackChannel.setStreamHandler(this);

    applicationContext = flutterPluginBinding.applicationContext
  }

  override fun onDetachedFromEngine(@NonNull binding: FlutterPlugin.FlutterPluginBinding) {
    applicationContext = null;

    channel.setMethodCallHandler(null)

    callBackChannel.setStreamHandler(null);
  }

  override fun onMethodCall(call: MethodCall, result: Result) {
    when (call.method) {
      "getVersion" -> version(result)
      "setLogLevel" -> setLogLevel(result, call)
      "addButton" -> addButton(result, call)
      "removeButton" -> removeButton()
      "showButton" -> showButton(result)
      "hideButton" -> hideButton(result)
      "activate" -> activate(result)
      "deactivate" -> deactivate(result)
      "callProjectApi" -> callScript(result, call)
      "setVisualState" -> setVisualState(result, call)
      "sendText" -> sendText(result, call)
      "playText" -> playText(result, call)
      "playCommand" -> playCommand(result, call)
      "isActive" -> isActive(result, call)
      "setWakewordEnabled" -> setWakewordEnabled(result, call)
      "getWakewordEnabled" -> getWakewordEnabled(result, call)
      else -> result.notImplemented()
    }
  }

  private fun setLogLevel(result: Result, call: MethodCall) {
    val logLevel = call.argument<String>(ARGUMENT_LOG_LEVEL)
    if (logLevel == "all") {
      AlanLogger.LogLevel.EVENTS
    }
    else {
      AlanLogger.LogLevel.BASIC
    }
    result.success(true)
  }

  private fun setVisualState(result: Result, call: MethodCall) {
    if (alanButton != null) {
      alanButton?.setVisualState(call.argument<String>(ARGUMENT_VISUALS))
      alanButton?.post {
        result.success(true)
      }
    }
  }

  private fun playCommand(result: Result, call: MethodCall) {
    if (alanButton != null) {
      var command = call.argument<String>(ARGUMENT_COMMAND)
      alanButton?.playCommand(command) { method, body, error ->
        alanButton?.post {
          result.success(listOf(method, body, error))
        }
      }
    }
  }

  private fun sendText(result: Result, call: MethodCall) {
    if (alanButton != null) {
      alanButton?.sendText(call.argument<String>(ARGUMENT_TEXT))
      alanButton?.post {
        result.success(true)
      }
    }
  }

  private fun playText(result: Result, call: MethodCall) {
    if (alanButton != null) {
      alanButton?.playText(call.argument<String>(ARGUMENT_TEXT))
      alanButton?.post {
        result.success(true)
      }
    }
  }

  private fun callScript(result: Result, call: MethodCall) {
    if (alanButton != null) {
      alanButton?.callProjectApi(call.argument<String>(ARGUMENT_METHOD_NAME),
              call.argument<String>(ARGUMENT_METHOD_ARGS)
      ) { method, body, error ->
        alanButton?.post {
          result.success(listOf(method, body, error))
        }
      }
    }
  }

  private fun version(result: Result) {
    if (alanButton != null) {
      result.success(alanButton?.sdk?.version ?: "")
    }
  }

  private fun setWakewordEnabled(result: Result, call: MethodCall) {
    if (alanButton != null) {
      val enabled = call.argument<Boolean>(ARGUMENT_WAKEWORD_ENABLED) ?: return
      result.success(alanButton?.setWakewordEnabled(enabled))
    }
  }

  private fun getWakewordEnabled(result: Result, call: MethodCall) {
    if (alanButton != null) {
      result.success(alanButton?.getWakewordEnabled())
    }
  }

  private fun isActive(result: Result, call: MethodCall) {
    if (alanButton != null) {
      result.success(alanButton?.isActive())
    }
  }

  private fun activate(result: Result) {
    alanButton?.activate()
    result.success(true)
  }

  private fun deactivate(result: Result) {
    alanButton?.deactivate()
    result.success(true)
  }

  private fun showButton(result: Result) {
    alanButton?.showButton()
    result.success(true)
  }

  private fun hideButton(result: Result) {
    alanButton?.hideButton()
    result.success(true)
  }

  private fun removeButton() {
    if (alanButton != null && activity != null) {
      val rootView = activity!!.findViewById(android.R.id.content) as ViewGroup
      alanButton?.sdk?.clearCallbacks()
      alanButton?.getSDK()?.stop()
      rootView.removeView(alanButton)
      alanButton = null
    }
  }

  private fun addButton(result: Result, call: MethodCall) {
    Alan.PLATFORM_SUFFIX = "flutter"
    Alan.PLATFORM_VERSION_SUFFIX = call.argument<String>(ARGUMENT_PLUGIN_VERSION)
    Alan.QR_EVENT_BUS_ENABLED = true

    val projectId = call.argument<String>(ARGUMENT_PROJECT_ID)
    val authJson = call.argument<String>(ARGUMENT_PROJECT_AUTH_JSON)
    val server = call.argument<String>(ARGUMENT_PROJECT_SERVER)

    if (projectId == null) {
      result.error("No projectId, please provide projectId argument", null, null)
      return
    }

    val config = AlanConfig.builder()
              .setProjectId(projectId)
              .setDataObject(authJson)

    if (!server.isNullOrEmpty()) {
      config.setServer("wss://" + server)
    }

    if (alanButton == null) {
      createButton(call, null)
      subscribe()
    }

    alanButton?.initWithConfig(config.build())

    subscribe()
    alanButton?.showButton()

    val align = call.argument<Int>(ARGUMENT_BUTTON_HORIZONTAL_ALIGN)
    if (align == AlanButton.BUTTON_LEFT) {
      alanButton?.setButtonAlign(AlanButton.BUTTON_LEFT)
    } else {
      alanButton?.setButtonAlign(AlanButton.BUTTON_RIGHT)
    }

    result.success(true)
    return
  }

  @Subscribe(threadMode = ThreadMode.ASYNC)
  fun onBarcodeEvent(event: BarcodeEvent?) {
    Handler(Looper.getMainLooper()).postDelayed({
      subscribe()
    }, 100)
  }

  override fun onListen(arguments: Any?, events: EventSink?) {
    sink = events;
  }

  override fun onCancel(arguments: Any?) {
    sink = null
  }

  inner class AlanEventSink : StreamHandler {

    override fun onListen(p0: Any?, p1: EventSink?) {
      sink = p1
    }

    override fun onCancel(p0: Any?) {
      sink = null
    }

    fun newAlanState(state: AlanState) {
      sink?.success(listOf("button_state_changed", state.name))
    }

    fun newCommand(payload: String) {
      sink?.success(listOf("command", payload))
    }

    fun newEvent(event: String, payload: String) {
      sink?.success(listOf("event", event, payload))
    }

    fun newOnButtonState(state: AlanState) {
      sink?.success(listOf("onButtonState", state.name))
    }

    fun newOnCommand(payload: String) {
      sink?.success(listOf("onCommand", payload))
    }

    fun newOnEvent(payload: String) {
      sink?.success(listOf("onEvent", payload))
    }
  }

  private fun subscribe() {

    callBackChannel.setStreamHandler(alanSink)

    alanButton?.registerCallback(object : AlanCallback() {
      override fun onAlanStateChanged(alanState: AlanState) {
        alanSink.newAlanState(alanState)
      }

      override fun onCommandReceived(eventCommand: EventCommand?) {
        alanSink.newCommand(eventCommand?.data?.getString("data") ?: "")
      }

      override fun onRecognizedEvent(eventRecognised: EventRecognised?) {
        val text = eventRecognised?.getText() ?: "";
        val final = eventRecognised?.isFinal() ?: false;
        alanSink.newEvent("recognized", "{\"text\":\"${text}\", \"final\":\"${final}\"}")
      }

      override fun onParsedEvent(eventParsed: EventParsed?) {
        val text = eventParsed?.getText() ?: "";
        alanSink.newEvent("parsed", "{\"text\":\"${text}\"}")
      }

      override fun onTextEvent(eventText: EventText?) {
        val text = eventText?.getText() ?: "";
        alanSink.newEvent("text", "{\"text\":\"${text}\"}")
      }

      override fun onEvent(event: String, payload: String) {
        alanSink.newEvent(event, payload)
      }

      override fun onEvent(payload: String) {
        alanSink.newOnEvent(payload)
      }

      override fun onCommand(eventCommand: EventCommand?) {
        alanSink.newOnCommand(eventCommand?.data?.getString("data") ?: "")
      }

      override fun onButtonState(alanState: AlanState) {
        alanSink.newOnButtonState(alanState)
      }
    })
  }

  private fun createButton(call: MethodCall, result: Result?) {
    if (activity == null) {
      return
    }

    val rootView = activity!!.findViewById(android.R.id.content) as ViewGroup
    alanButton = AlanButton(activity, null)
    val params = FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.WRAP_CONTENT)
    params.gravity = Gravity.BOTTOM or Gravity.END
    alanButton?.let {
      it.layoutParams = params
      rootView.addView(it)
    }

    result?.success(true)
  }

}
