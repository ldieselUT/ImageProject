CC=g++
CFLAGS+=`pkg-config --cflags opencv` -std=c++11
LDFLAGS+=`pkg-config --libs opencv`

PROG=watermarking
OBJS=$(PROG).o

.PHONY: all clean
$(PROG): $(OBJS)
	$(CC) -o $(PROG) $(OBJS) $(LDFLAGS)

%.o: %.cpp
	$(CC) -c $(CFLAGS) $<

all: $(PROG)

clean:
	rm -f $(OBJS) $(PROG)
