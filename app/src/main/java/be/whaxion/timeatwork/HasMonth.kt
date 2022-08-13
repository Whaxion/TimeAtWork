package be.whaxion.timeatwork

import androidx.fragment.app.Fragment

abstract class HasMonth : Fragment() {
    abstract fun nextMonth()
    abstract fun previousMonth()
    abstract fun close()
}