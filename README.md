Shout for Android
=================

> Twitter for people that you don't know that you know.

Compiling
---------

1. Shout requires some 3rd-party API keys, specified in the
   shout-release repository.  Ensure that a copy of it is in the same
   parent directory as this repo.

   1.1 Follow the instructions in the shout-release README to
       configure it.

2. Shout uses MANES libraries, so clone the MANES repository and
   imported the MacLib and MacEntity into your Eclipse before
   executing the following steps.

   2.1 Import the Shout and ShoutTest projects located in project/ as
       existing Eclipse projects

   2.2 Set up a run configuration to run the unit tests by following
       the instructions at
       [here](http://pivotal.github.com/robolectric/eclipse-quick-start.html).

3. Shout requires 3rd party libraries contained as symlinks or
   submodules in the project/contrib directory.
  
   3.1 Follow the the instructions in project/contrib/README to import
       these libraries into your Eclipse workspace

4. To run Shout on an emulator, use an AVD with the 2.2 platform or
   newer.
