package me.yuriisoft.buildnotify.mobile.feature.activebuilds.domain.model.events

/**
 * One build-related message from the IDE, already translated from the wire format.
 *
 * Events are plain data. They are applied to the local store in batches by the data
 * layer; nothing folds them into ever-growing in-memory lists.
 */
sealed interface BuildEvent
