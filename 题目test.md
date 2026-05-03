n faraway Boboland, a king election is held every five years. This year is the time for another king election in Boboland. Each city in Boboland has nominated nn**n** king candidates, numbered 1,2,…,n1,2,\\dots,n**1**,**2**,**…**,**n**. These nn**n** candidates have **distinct** political tendencies a1,a2,…,ana\_1,a\_2,\\dots,a\_n**a**1****,**a**2****,**…**,**a**n**** (1≤ai≤1091\\leq a\_i\\leq 10^9**1**≤**a**i****≤**1**0**9** represents the political tendency of the ii**i**-th candidate, where a larger number implies a more right-wing tendency, 11**1** represents extreme left, and 10910^9**1**0**9** represents extreme right). Then, the following internal voting mechanism will be conducted among the candidates to decide the final king:

* There will be n−1n-1**n**−**1** rounds of voting, and exactly one candidate will be eliminated in each round until there is only one candidate left, who will become the final king.

  *   	The voting rule for each round is as follows: each candidate can vote for any other candidate except for themselves. The candidate with the most votes will be eliminated. If there are multiple candidates with the same highest number of votes, the one among them with the **rightmost** tendency will be eliminated.
    After observing all previous king elections in Boboland, you found that each candidate adheres to the principle of attacking opponents with different opinions and will execute the following strategy in each round of voting:

    Among all remaining candidates, vote for the candidate whose political tendency is most different from their own (i.e., the ii**i**-th candidate, if they have not been eliminated, will vote for the jj**j**-th candidate with the largest ∣aj−ai∣|a\_j-a\_i|**∣**a**j****−**a**i****∣**, who has not been eliminated). If there are multiple candidates with the largest ∣aj−ai∣|a\_j-a\_i|**∣**a**j****−**a**i****∣**, they will vote for the one among them with the **rightmost** tendency.
  Now you want to know who will become the final king in this year's election in Boboland.

  ## 输入描述:

  ```
  The first line contains a positive integer n (1≤n≤106), denoting the number of candidates.

  The second line contains n distinct integers a1,a2,…,an (1≤ai≤109), denoting the political inclination of each candidate.
  ```
  ## 输出描述:

  ```
  Output an integer in a line, which represents the candidate's number who will eventually become the king.
  ```
                           示例1                       
  ## 输入

  [复制]()4 5 1 8 10

  ```
  4
  5 1 8 10
  ```
  ## 输出

  [复制]()1

  ```
  1
  ```
