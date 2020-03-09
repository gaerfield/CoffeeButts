# CoffeeButts
Kotlin training ground

## Communication between Components

```puml
actor customer
actor frontdesk
actor barista

customer --> frontdesk : order

== processing order ==

frontdesk --> barista : ordered
barista --> barista : processing
barista --> frontdesk : created

== cashing customer ==
frontdesk --> customer : invoicing
customer --> frontdesk : paying

== delivering coffee ==
frontdesk --> customer : coffee

```

## State of overall order
```puml

state ordered
state delivered

[*] -> ordered : order
ordered -> delivered : paid && created
delivered -> [*]

```
### state of payment
```puml

state ordered
state invoiced
state paid

ordered -> invoiced : invoice
invoiced -> paid : paying

```
### state of processing
```puml

state ordered
state processing
state created

ordered -> processing : barista launched
processing -> created : barista finished

```
