# CoffeeButts

This project has emerged from a personal exercise focusing on developing a backend using the following techniques and technology:

* use domain-driven-architecture (meaning aggregates and domain-events)
* make it reactive:
  * using spring-boot
  * using kotlin and coroutines

It also contains a "classic" imperative approach, so a comparison in code-complexity and mantainability is possible.

## Problem-Description

Implement a coffee-Bar that:

1. allow customers to order multiple coffees at once
2. pay the bill
3. and receive the ordered coffees.
4. coffees can be of the type: Cappucino, Latte Machiatto, Espresso or Double Espresso
5. the customer can order multiple coffees of the same type

The coffee-Bar is mantained by two persons:

6. the frontdesk will receive orders and and let the customer pay the bill
7. the barista will produce all coffees sequentially
8. every coffee will take about 2.5s to be created

At the end of the day/week/month/year:

9. statistics should be calculated (most requested coffees, number of orders, ...)

The two employees are hired, to speed-up the coffee-production. This means:

11. The barista starts his work, regardless if the bill is already paid.
12. With succesfull payment the customer is allowed to receive produced coffees, regardless if the whole order is processed completely.
13. A produced coffee only can be received once by a customer (the customer should not be able to receive new coffees by displaying the same order-id)

Of course, the problem is somewhat artificially. The exercise is meant to cover traditional crud-requirements (see 9) as well as asynchronous calculations (producer-consumer to solve 12).

## Solution

In the following I give an Introduction, how I approached the solution of the task. Of course, may approach is only one solution and probably not the optimal one. Whatever, here are my thoughts.

### Event-Storming

Because the Event-Storming has happened rudimentary scribbled on a blank page, I reduce the results (instead of issueing a photo with unreadable handwriting) by only giving you the Events, I'd identified:

* Ordered: the customer issued the order-coffee-command
* Invoiced: the bill is created
* Paid: the bill is paid
* Processing: the barista has started creating coffees
* Processed: the barista has finished creating coffees
* Delivered: all coffees had been delivered to the customer

### Communication-Diagram

The communication between all components should something like this

![communications-diagram](http://www.plantuml.com/plantuml/proxy?cache=no&src=https://raw.githubusercontent.com/gaerfield/CoffeeButts/master/doc/uml/communications-diagram.puml)

Remember: the cashing of the customer and the creation of coffees happens asynchronously.

### States

Besides the components the following states

![state-diagram](http://www.plantuml.com/plantuml/proxy?cache=no&src=https://raw.githubusercontent.com/gaerfield/CoffeeButts/master/doc/uml/state-diagram.puml)

## Problems during implementation

* spring-data:
  * with [r2dbc](https://r2dbc.io/): skipped that, because only flat objects
  * with experimental "inline class" is currently not possible because of [DATACMNS-1517](https://jira.spring.io/browse/DATACMNS-1517)
* swagger
  * not generated when using [router](https://docs.spring.io/spring/docs/current/spring-framework-reference/web-reactive.html#webflux-fn-router-functions)
  * incorrect examples when using kotlins flow [springdoc-openapi#159](https://github.com/springdoc/springdoc-openapi/issues/159)
  * put simply: "spring-webflux + kotlin = no swagger"