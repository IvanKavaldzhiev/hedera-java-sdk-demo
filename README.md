# Hedera Demo Application

> A simple demo application that demonstrates how to use the Hedera Java SDK to create simple use cases


## Install

The project uses gradle, so you can use:

```
./gradlew clean build
```

## Setup

In order to run the demo examples successfully, you need to create your own account on the environment you would point your execution to.
You can do this in the Hedera Portal: https://portal.hedera.com/

### Examples

Requires `OPERATOR_ID` and `OPERATOR_KEY` to be in a .env file in the root directory. By default examples run against
the Hedera test network, but the network could be overridden by setting `HEDERA_NETWORK` property.

## Usage

Run the main method of the demo you want to try out and examine the results in the logs.

## Try it out

We welcome all developers or users that are curious how to interact with Hedera to give this demo a try