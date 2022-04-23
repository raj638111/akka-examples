

# Why needed?

  - **Overflow:** 
    - Implementing streaming with actor alone is error-prone (Got to ensure we do not overflow buffers or mailboxes)
  - **Lost messages:**
    - Actor messages can be lost and need to be retransmitted

--- 

# Features

  - Can **limit buffering**
  - Provides **Back pressure**
    - Slows down producers if consumers cannot keep up



