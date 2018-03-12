# Mail Bot Blues

## Overview
You, an independent Software Contractor, have been hired by Robotic Mailing Solutions Inc. (RMS) to provide
some much needed assistance in delivering the latest version of their product Automail to market. The Automail
is an automated mail sorting and delivery system designed to operate in large buildings that have dedicated
Mail rooms. It offers end to end receipt and delivery of all mail within the large building, and can be tweaked to
fit many different installation environments. The system consists of four key components:
- **A Mail Pool** system which can hold a number of mail items which have arrived at the building.
- **A Delivery Robot** (see Figure 1) which delivers mail items from the mail pool throughout the building.
- **A Storage Unit**, that is, a ‘backpack’ which is attached to the delivery robot. It can contain at most
four mail items; these mail items are in delivery order.
- **A Mail Selecting** system which decides what mail items should go into a robot’s backpack for delivery
and in what order, and in some cases which items should come out.  

The hardware of this system has been well tested, and the system engineers have confidence in their design.
Recently a new robot was acquired which removed the mail item weight limitation of the earlier model. RMS
wants to run two robots together, an old one and a new one, for a more effective solution. Unfortunately, the
performance seen so far still seems well below optimal, and sometimes results in heavy items being given to
the weak robot. RMS has traditionally been a hardware company, and as such do not have much software
development experience. As a result, the strategies that they are using to organise the mail and select the mail
for delivery are very poor.  

Your job is to apply your software engineering knowledge to fix the mail sorting and to develop a better strategy
for selecting the mail for delivery, all with the aim to improve the performance of their system. Once you have
made your changes, they will be benchmarked to provide feedback on your performance to Robotic Mailing

## Useful Information
- The mailroom is on the ground floor.
- All mail items are stamped with their time of arrival.
- Some mail items are priority mail items, and carry a priority of either 100 high or 10 low.
- Priority mail items are delivered and registered one at a time, so timestamps are unique for priority
items.
- Normal (non-priority) mail items are delivered in a batches; all items in a normal batch receive the
same timestamp.
- A Delivery Robot carries a Storage Unit which can contain at most four mail items.
- A Delivery Robot can be sent to deliver mail even if the Storage Unit is not full.
- A Delivery Robot delivers the mail in the order it appears in the storage unit.
- All mail items have a weight from 200 to 5000 grams; the weak delivery robot can only deliver items
up to 2000 grams.
- The system is judged on the sum across all mail items of a measure of the time taken to deliver the item
  - the measure has a power factor of 1.1 to make late items more urgent, and is scaled up for priority
items by square root of the priority (e.g. 11 times for priority 100)
  - Specifically: (delivery time - arrivalTime)ˆ1.1 * (1+sqrt(priority))

## Strategies Package Diagram
Seen below is a diagram of the strategies interfaces and related elements. Be aware that this does not represent
all the elements in Simulation. This diagram is provided below to aid in the understanding of the application.  
[Strategy Package](strategy-package.png)

## Task
As you will notice, the current strategies used for sorting mail are incorrect and the current strategies for selecting
mail and responding to priority arrivals are simplistic and not optimised for any particular use case. Thankfully,
Robotic Mailing Solutions Inc. has made use of the Strategy pattern (see reference here) to make their software
more flexible, by creating a common interface for dealing with mail in the mailpool and for the robot behaviour
in selecting mail.  

Your task is to develop new sorting and selecting strategies for mail within the Automail system. Robotic Mailing
Solutions inc. is interested in correcting the MailPool, and improving the RobotBehaviour, as internal testing
has shown these to be a significant factor in limiting current performance. Specifically you must:
1. Create a class MyMailPool in the strategies package implementing the IMailPool interface.
2. Create a class MyRobotBehaviour in the strategies package implementing the IRobotBehaviour interface.
3. Modify Automail.java to use these classes (code already provided in the file as comments).
4. Test your modified version of the system (including on a University lab computer to ensure compatibility).
5. Ensure all your code is commented and of good quality (see below)

You must include in your code, comments explaining the rationale for your data structure and algorithm choices
and how these work toward achieving your goals for the MyMailPool and MyRobotBehaviour implementations.
It is important to note that the strategies you provide must be different from those provided to you in the
sample package, and must achieve a better (lower) value for the “Final Score” statistic.


