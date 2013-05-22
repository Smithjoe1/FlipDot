FlipDot
=======

Flip dot display server software

This software has been written to drive the AlfaZeta flipdot display from http://www.flipdots.com/, playing a series of GIFs and hard coded animations, which have had to have been removed.

It loads the number of panels from config.txt and the serial port to pipe the RS485 commands through.

It then looks at config.txt for the order of the pattern to be shown. With the first several options going to the hard coded animations.

It uses processing for it's serial RXTX installation and image processing utilities and will need to be added as a library in the IDE.
