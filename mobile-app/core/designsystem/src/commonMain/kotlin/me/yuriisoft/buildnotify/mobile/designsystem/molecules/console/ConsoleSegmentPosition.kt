package me.yuriisoft.buildnotify.mobile.designsystem.molecules.console



/** Where a [ConsoleSegment] sits in a lazily rendered console. */
enum class ConsoleSegmentPosition(val isFirst: Boolean, val isLast: Boolean) {
    Single(isFirst = true, isLast = true),
    First(isFirst = true, isLast = false),
    Middle(isFirst = false, isLast = false),
    Last(isFirst = false, isLast = true),
    ;

    companion object {
        fun of(index: Int, count: Int): ConsoleSegmentPosition = when {
            count <= 1         -> Single
            index == 0         -> First
            index == count - 1 -> Last
            else               -> Middle
        }
    }
}
