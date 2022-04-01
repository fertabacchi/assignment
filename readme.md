# project structure
we proceed with a description of the project module by module.
- the **api** module declares the set of interfaces representing the outside world (reference data, market data,
positions etc) and basic objects associated. It does not contain any logic or implementation
and classes which 
- the **core** contains the service core logic; it must be provided the implementations of the interfaces
defined in the *api* module. It can start, consuming data from them and will produce notifications containing 
portfolio updates for the subscribers
- the **app** is our running service, implementing all interfaces in *api* and initialising the *core* service

P.S. given to being a complete beginner in Gradle, I was not able to effectively setup the desired multi-module project
with their dependencies.
The classes are split in 3 main packages which virtually reflect what the module structure should have been like.

## API module
the API module declares the interfaces:
- **reference data service**: given a ticker it returns the instrument description, which could be either an Equity or an Option.
Interface-wise, this is the most static component of the architecture and data is not expected to change.
- **position service**: given a timestamp, provides the open positions (the instrument ticker and number of contracts/shares).
- **market service**: given an equity ticker and a timestamp, provides the market price of the equity. No bid/ask or market closure information are available.
- **option price service**: calculates the value of an EU option, given the Option reference data, the equity's price, and the pricing timestamp. While
the *market* reflects a communication with the external world, this pricing service is
expected to only do static computations

Plus, we specify what kind of data subscribers will receive.
From the document, it is not clear if listeners are aware of equity price 
updates because they're in their portfolio or if that's a separate feed (on top of the portfolio information).
I suppose it makes sense that if we have an open position for an option but not for its underlying equity, they'd still
want to get the update containing the equity price.
In order to keep it simple, each portfolio update will also contain the full view of equity prices which are involved
in the option calculations.

##CORE module
This module contains the PortfolioService which starts and coordinate the whole process.
Subscribers can register themselves and they'll be allocated a queue to consume portfolio updates from.
Repeatedly, the service fetches all the tickers from the open positions, retrieves the static data and uses it to
calculate the prices. Finally, it publishes the update to the interested subscribers.
Since we assume that reference data is immutable, the information about equities and options is cached.

This portfolio service entirely dictates the timing of the price sequencing, so that the virtual timestamp of the 
portfolio is a parameter sent to the market-service, to the option-pricing-service and to the position-service.
This approach was preferred to a "real time" scenario, also so that that subscribers 
can receive time-consistent snapshots.
So in this solution the time is just emulated, being a parameter of all the interfaces.
Among other things, the time interval of the snapshot refresh is a parameter of the constructor,
plus another optional time interval which is the "real" time the service should wait between one notification and another.
Basically the service will produce portfolio updates each X seconds, pretending that Y seconds have passed instead
(X and Y can be the same, of course, reproducing a real-time experience)

##APP module
this contains mainly the custom implementation of the interfaces defined in the *api* module, plus starts the 
actual service. 
The implementations follow the guidelines of the document: positions from a CSV, brownian motion equity price
simulator, Black-Scholes option modelling, and H2 database for reference data.
The service is started from here after initialising all relevant services and providing the mock data.

# notes
##thread safety
The classes throughout are partially thread safe. For example the brownian market service is not thread safe, 
but the core service uses only one thread to pull the positions or compute the prices, so it won't
be an issue.

The *core* module itself is thread safe and overall we can say the project is thread-safe. In particular it
is able to manage updates with several subscribers in a consistent way (e.g. BlockingQueue) while a
detached thread is running the PortfolioService. An improved version of it would likely calculate prices in parallel across 
many instruments.


##numbers and precision
Users will receive BigInteger for the number of positions and BigDecimal for prices. Internally, calculations
are performed with a combination of doubles and BigDecimal; doubles are more efficient and are preferred for 
figuring out multiplication factors, when they're about to be multiplied by prices they're transformed in BigDecimal.
Internal BigDecimal calculations keep a precision of 13, reduced to 10 when we return the final computation to users.

##database
the H2 database works with only two tables. One for equities and one for options. The option records own a FK
towards their underlying equities. The mock "reference data service" can tell which table should be looked up to
find a ticker by checking the format of the ticker. This lightweight approach was preferred to a proper table hierarchy
which would be fit in case we handle many types of financial instruments.

##cumulative probability calculation
since I didn't quite know how to compute the cumulative probability in a normal distribution,
I used a dump sampling approach. By generating a few thousands random gaussian numbers and arranging them in a 
SortedSet, finding the cumulative odds up till X means figuring out how which fraction of the random samples
is less than X. Hence, after initialisation, each operation takes O(log(n)) where n is the number of samples.

# coding / execution notes
- completing the assignment took me about 3 full working days, including the time for getting started with Gradle basics.
- plenty of javadoc throughout should clarify the scope of each class
- the main class is *com.crypto.portfolio.app.Runner*. Running "gradle build" will generate an executable fat-jar
for java 1.8, which can be simply run by *java -jar portfolio-1.0-SNAPSHOT.jar*


