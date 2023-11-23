# Rotating Pairs Kata
The goal is everytime we rotate, to always maximize having unique pairs, and to minimize being on the same codebase.  
Theoretically we can rotate 6 times in a timeframe of 90' with intervals of 15'.  
Or we can rotate 5 times in a timeframe of 100' with intervals of 20'.

## Basics
Let's assume we'll always have an **even number** of People, so we can always form Pairs of 2 People.  

There are specific rules when pairing up initially, easiest is to just form Pairs with your neighbour:

    Given people "Silvio", "Olivier", "Florent", "Jeremie", "Jelle", "Agnes"
    when pairing up
    then the following Pairs should be formed:
    Codebase 1 with "Silvio" and "Olivier",
    Codebase 2 with "Florent" and "Jeremie",
    Codebase 3 with "Jelle" and "Agnes",

Rules when rotating:
1. First, make sure the Pair itself is unique (People in the Pairs haven't paired yet in this Kata).
2. Secondly, make sure at least one of the People in the Pair is working on a Codebase they haven't seen yet.
3. Third, make sure one person in a Pair stays for at least one rotation (or worded otherwise: a codebase should never be a complete new one for a whole Pair)
4. If none of the above rules can be applied anymore, the rotation starts over from the top so it's the same exact constellation as the initial pair-up. (The way we continue the rotation is arbitrary, but you can consider this an enabling constraint)

Here are the acceptance criteria for a kata with 6 People, applying only the rules of having unique pairs:

    Given people "Silvio", "Olivier", "Florent", "Jeremie", "Jelle", "Agnes"
    when pairing up
    then the following Pairs should be formed:
    "Silvio" and "Olivier",
    "Florent" and "Jeremie",
    "Jelle" and "Agnes",

    when rotating for the 1st time
    then the following Pairs should be formed:
    "Silvio" and "Jeremie",
    "Florent" and "Agnes",
    "Jelle" and "Olivier"
    
    when rotating for the 2nd time
    then the following Pairs should be formed:
    "Silvio" and "Agnes",
    "Florent" and "Olivier",
    "Jelle" and "Jeremie"    

    when rotating for the 3rd time
    then the following Pairs should be formed:
    "Silvio" and "Olivier",
    "Florent" and "Jeremie",
    "Jelle" and "Agnes",

Here are the acceptance criteria for a kata with 6 People, applying all the rules:

    Given people "Silvio", "Olivier", "Florent", "Jeremie", "Jelle", "Agnes"
    when pairing up
    then the following Pairs should be formed:
    Codebase 1 with "Silvio" and "Olivier",
    Codebase 2 with "Florent" and "Jeremie",
    Codebase 3 with "Jelle" and "Agnes",
    
    when rotating for the 1st time
    then the following Pairs should be formed:
    Codebase 1 with "Silvio" and "Jeremie",
    Codebase 2 with "Florent" and "Agnes",
    Codebase 3 with "Jelle" and "Olivier",
    
    when rotating for the 2nd time
    then the following Pairs should be formed:
    Codebase 1 with "Jeremie" and "Jelle",
    Codebase 2 with "Agnes" and "Silvio",
    Codebase 3 with "Olivier" and "Florent",
    
    when rotating for the 3rd time
    then the following Pairs should be formed:
    Codebase 1 with "Jelle" and "Agnes",
    Codebase 2 with "Silvio" and "Olivier",
    Codebase 3 with "Florent" and "Jeremie",
    //Even though these are not unique pairs (initial pair-up), they'll be working on a codebase that's completely new to them
    
    when rotating for the 4th time
    then the following Pairs should be formed:
    Codebase 1 with "Agnes" and "Florent",
    Codebase 2 with "Olivier" and "Jelle",
    Codebase 3 with "Jeremie" and "Silvio",
    //Again, the pairs are no longer unique (same as 1st rotation), but they're working on a codebase they didn't start on
    
    when rotating for the 5th time
    then the following Pairs should be formed:
    Codebase 1 with "Florent" and "Olivier",
    Codebase 2 with "Jelle" and "Jeremie",
    Codebase 3 with "Silvio" and "Agnes",
    //Again, the pairs are no longer unique (same as 2nd rotation), but they're working on a codebase they didn't start on
    
    when rotating for the 6th time
    then the following Pairs should be formed:
    Codebase 1 with "Olivier" and "Silvio",
    Codebase 2 with "Jeremie" and "Florent",
    Codebase 3 with "Agnes" and "Jelle",
    //Finally, none of the pairs are unique, and they'll be working on the same codebase they started on (same as in the initial pair-up)

    when rotating for the 7th time
    then the following Pairs should be formed:
    Codebase 1 with "Silvio" and "Jeremie",
    Codebase 2 with "Florent" and "Agnes",
    Codebase 3 with "Jelle" and "Olivier",
    //Since we completed all the possible constellations, we start over from the top (so 1st rotation after pair up)

## Extension
When dealing with an **odd number** of People, there will be at least one Triplet among the Ensembles, all the others are still Pairs.  
The same rules should still apply when rotating:
1. First, make sure the Pair itself is unique (People in the Pairs haven't paired yet in this Kata).
2. Secondly, make sure at least one of the People in the Pair is working on a Codebase they haven't seen yet.
3. If neither the first nor the second rule can be applied anymore, the rotation starts over from the top so it's the same exact constellation as the initial pair-up. (The way we continue the rotation is arbitrary, but you can consider this an enabling constraint)
