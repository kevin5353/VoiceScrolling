package com.aaron.voicescrolling.service

import android.accessibilityservice.AccessibilityService
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import android.content.Context
class ScrollAccessibilityService : AccessibilityService() {

    private lateinit var voiceRecognizer: VoiceRecognizer

    override fun onServiceConnected() {
        super.onServiceConnected()
        voiceRecognizer = VoiceRecognizer(this) { command ->
            handleVoiceCommand(command)
        }
        voiceRecognizer.startListening()
    }

    private fun handleVoiceCommand(command: String) {
        android.widget.Toast.makeText(this, "Command: $command", android.widget.Toast.LENGTH_SHORT).show()
        when {
            command.contains("down") -> performScroll(true)
            command.contains("up") -> performScroll(false)
        }
    }

    private fun performScroll(down: Boolean) {
        val displayMetrics = resources.displayMetrics
        val screenHeight = displayMetrics.heightPixels
        val screenWidth = displayMetrics.widthPixels

        val startX = screenWidth / 2f
        val startY = if (down) screenHeight * 0.7f else screenHeight * 0.3f
        val endY = if (down) screenHeight * 0.3f else screenHeight * 0.7f

        val path = android.graphics.Path().apply {
            moveTo(startX, startY)
            lineTo(startX, endY)
        }

        val gestureBuilder = android.accessibilityservice.GestureDescription.Builder()
        val strokeDescription = android.accessibilityservice.GestureDescription.StrokeDescription(path, 0, 400)
        gestureBuilder.addStroke(strokeDescription)

        // CAPTURE THE RESULT
        val dispatched = dispatchGesture(gestureBuilder.build(), object : AccessibilityService.GestureResultCallback() {
            override fun onCompleted(gestureDescription: android.accessibilityservice.GestureDescription?) {
                android.util.Log.d("Voice", "Gesture Callback: COMPLETED")
            }
            override fun onCancelled(gestureDescription: android.accessibilityservice.GestureDescription?) {
                android.util.Log.e("Voice", "Gesture Callback: CANCELLED")
            }
        }, null)

        // LOG IF IT FAILED TO START
        if (!dispatched) {
            android.util.Log.e("Voice", "CRITICAL ERROR: Gesture failed to dispatch! Check Manifest meta-data.")
        } else {
            android.util.Log.d("Voice", "Gesture dispatched successfully...")
        }
    }
    private fun findScrollableNode(root: AccessibilityNodeInfo?): AccessibilityNodeInfo? {
        if (root == null) return null

        // Check if this node specifically supports scrolling actions
        val actions = root.actionList
        if (actions.any { it.id == AccessibilityNodeInfo.ACTION_SCROLL_FORWARD || it.id == AccessibilityNodeInfo.ACTION_SCROLL_BACKWARD }) {
            return root
        }

        // Recursively check all children
        for (i in 0 until root.childCount) {
            val child = root.getChild(i)
            val result = findScrollableNode(child)
            if (result != null) return result
        }
        return null
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {}
    override fun onInterrupt() { voiceRecognizer.stopListening() }

    override fun onDestroy() {
        super.onDestroy()
        voiceRecognizer.stopListening()
    }
}