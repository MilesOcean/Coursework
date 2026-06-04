# Reflection

The AI generated the first draft of the design, which I committed
as the AI version. I then revised it myself, and the differences
between the two git commits show my own contribution.

The AI was useful for scaffolding, but it also introduced errors
(e.g. mixing HeroType/HeroClass, duplicated fields, an illegal
static method in an interface). I identified and fixed these
manually, so the final design reflects my own decisions rather
than unverified AI output.

I generated the design and model layers with the AI in separate,
scoped steps (design first, then model classes only). Working
layer by layer let me review and commit each part before continuing,
rather than accepting one large unverified output.

