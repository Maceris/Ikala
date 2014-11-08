package com.ikalagaming.event;

import java.lang.reflect.Array;
import java.util.Collection;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Queue;

/**
 * The queue that events are placed in pending dispatch.
 * @author Ches Burks
 *
 */
public class EventQueue implements Queue<Event>{

	/**Contains all of the data used in the queue**/
	private Event[] array = new Event[0];
	/**How many events are in the queue**/
	private int size = 0;
	/**Index of the next item that will be fetched**/
	private int head = 0;
	/**Position where the next element will be placed**/
	private int tail = 0;
	/**
	 * The percentage, represented from 0 to 100 as an int,
	 * that should be unused in the array before the contents are
	 * shifted to the beginning instead of resizing the array upon
	 * adding an element.
	 */
	private int shiftPercentageUnused = 50;
	/**The maximum number of slots that can be allocated.
	 * This is to prevent the queue from using up too much memory
	 */
	private final int maxArraySize = 8192;

	/**
	 * Inserts the specified element into this queue if it is possible to do
	 * so immediately without violating capacity restrictions, returning
	 * true upon success and throwing an IllegalStateException if no
	 * space is currently available.
	 *
	 * @param event the element to add
	 * @return true if this collection changed as a result of the call
	 * @throws IllegalStateException if the element cannot be added at this
	 * time due to capacity restrictions
	 * @throws NullPointerException if the specified element is null
	 */
	@Override
	public synchronized boolean add(Event event)
			throws IllegalStateException, NullPointerException{
		if (event == null){
			//its null so don't worry about adding it
			throw new NullPointerException();
		}
		if (size >= maxArraySize && getTotalFreeElements() <= 0){
			//its completely full and cant resize larger
			throw new IllegalStateException();
		}

		//shift the array and resize if necessary
		if (shouldShift(1)){
			shiftToStart();
		}
		while (getFreeElementsAtEnd() < 1){
			//don't double size if its got a free space, but do double if full
			doubleSize();
		}

		//copy elements over
		try {
			array[tail] = event;
			++tail;

			return true;
		}
		catch (Exception e){
			return false;//it failed copying
		}
	}

	/**
	 * Adds all of the events in the given Collection to the
	 * end of the queue.
	 * Elements are added in the order they appear in the supplied
	 * collection.
	 *
	 * @param events A list of events to add to the queue.
	 * @return true on success, false in the event of a failure
	 */
	@Override
	public synchronized boolean addAll(Collection<? extends Event> events) {
		//we don't need to do anything if the collections empty
		if (events.isEmpty()){
			return true;
		}
		if (events.size() > maxArraySize){
			//could not hold the events even if the queue is empty
			return false;
		}
		else if (events.size() == maxArraySize && !isEmpty()){
			//it could hold the events but its not empty so it will not
			return false;
		}
		//shift the array and resize if necessary
		if (shouldShift(events.size())){
			shiftToStart();
		}
		while (getFreeElementsAtEnd() < events.size()){
			//only double if there is not enough room for the array
			doubleSize();
		}

		//copy elements over
		try {
			Object[] eventArray = events.toArray();
			for (int i = 0; i < eventArray.length; ++i){
				array[i+tail] = (Event) eventArray[i];
			}
			//update tail
			tail += events.size();
			return true;
		}
		catch (Exception e){
			return false;//it failed copying
		}
	}

	/**
	 * Removes all of the elements from this collection (optional operation).
	 * The collection will be empty after this method returns.
	 * This does not actually clear out the data, only resets the head and tail
	 * positions.
	 */
	@Override
	public synchronized void clear() {
		head = 0;
		tail = 0;
	}

	/**
	 * Returns true if the queue contains the specified element and it has not
	 * already been dispatched. More formally, returns true if and only if
	 * this queue contains at least one element e such that
	 * (o==null ? e==null : o.equals(e)).
	 */
	@Override
	public synchronized boolean contains(Object object) {
		if (isEmpty()){
			return false;
		}
		for (int i = head; i < tail; ++i){
			//if both are null or if the objects are not null and are equal
			if (object == null ? array[i] == null : array[i].equals(object)){
				return true;
			}
		}
		return false;
	}

	/**
	 * Returns true if this collection contains all of the elements in
	 * the specified collection.
	 *
	 * @param objects collection to be checked for containment in this
	 * collection
	 * @return true if this collection contains all of the elements in the
	 * specified collection
	 */
	@Override
	public synchronized boolean containsAll(Collection<?> objects) {
		if (isEmpty()){
			return false;
		}
		boolean contains = true;
		boolean ranLoop = false;//if this is false, contains is false
		for (int i = head; i < tail; ++i){
			//check all objects in the objects collection
			for (Object object : objects){
				//if both are null or if the objects are not null and are equal
				if (object == null
						? array[i] == null
						: array[i].equals(object)){
					contains = contains && true;
				}
				else{
					contains = false;
				}
				if (!ranLoop){
					ranLoop = true;
				}
			}
		}
		return contains && ranLoop;
	}

	/**
	 * Doubles the size of the array and copies the new values over
	 * to the new array. If the array has a size of zero, it will set the
	 * size to one before doubling (resulting in a size of 2).
	 *
	 * @return true if resizing was successful, false if there was an error
	 */
	private synchronized boolean doubleSize(){
		try {
			if (size == 0){
				++size;
			}
			if (size >= maxArraySize){
				return false;
			}

			Event[] tmp = new Event[size*2];

			/* Copy from array (old data) starting at the old head
			 * position to the new array tmp, beginning at the index 0 of tmp,
			 * of length (tail-head).
			 */
			System.arraycopy(array, head, tmp, 0, tail - head);

			//set array to the new array
			array = tmp;
			tmp = null;

			size *= 2;
			//shift the head and tail over to the start
			tail -= head;
			head = 0;
			return true;
		}
		catch (Exception e){
			e.printStackTrace(System.err);
			return false;
		}
	}

	/**
	 * Retrieves, but does not remove, the head of this queue.
	 * This method differs from {@link #peek} only in that it throws an
	 * exception if this queue is empty.
	 * @return the head of this queue
	 * @throws NoSuchElementException if this queue is empty
	 */
	@Override
	public synchronized Event element() throws NoSuchElementException{
		if (isEmpty()){
			throw new NoSuchElementException();
		}
		return array[head];
	}

	/**
	 * Returns the event at the given position of the internal array.
	 * This is not intended for use outside of the iterator. May return null.
	 *
	 * @param position what position in the queue to retrieve from
	 * @return The element at that position
	 */
	private synchronized Event get(int position){
		if (position >= size){
			return null;
		}
		return array[position];
	}

	/**
	 * Returns the number of free elements between
	 * the beginning of the array and the head element.
	 *
	 * @return free spaces available before head
	 */
	private synchronized int getFreeElementsAtBeginning(){
		return head;
	}

	/**
	 * Returns the number of free elements between
	 * the tail and the end of the array.
	 *
	 * @return free spaces available after tail
	 */
	private synchronized int getFreeElementsAtEnd(){
		return size-tail;
	}

	/**
	 * Returns the number of free elements in
	 * the array. These can be before or after
	 * the block of elements currently being used.
	 *
	 * @return total free spaces available
	 */
	private synchronized int getTotalFreeElements(){
		return getFreeElementsAtBeginning()+getFreeElementsAtEnd();
	}

	/**
	 * Returns true if there are no more events
	 * left to be processed. Note that this will
	 * return true if there is data stored in the
	 * queue which has already been fetched and
	 * is not going to be used again, even though
	 * the data structure is not actually empty.
	 *
	 * @return false if there are unprocessed events,
	 * true otherwise
	 */
	@Override
	public synchronized boolean isEmpty() {
		if (head == tail){
			clear();
			return true;
		}
		//last event has been used
		else if (head>tail){
			clear();
			return true;
		}
		//not a valid index for head
		if (head <= -1){
			clear();
			return true;
		}
		//edge case, this should not happen
		if (tail <= -1){
			clear();
			return true;
		}
		return false;
	}

	/**
	 * Returns an iterator over the valid events in this collection.
	 *
	 * @return an Iterator over the elements in this collection
	 */
	@Override
	public synchronized Iterator<Event> iterator(){
		return new Iterator<Event>() {
			int pos = head;
			int end = tail -1;//last valid entry

			/**
			 * Returns true if the iteration has more elements.
			 * (In other words, returns true if {@link #next} would return
			 * an element rather than throwing an exception.)
			 *
			 * @return true if the iteration has more elements
			 */
			@Override
			public boolean hasNext() {
				//the next position is valid and next element is not null
				if (pos <= end && get(pos) != null){
					return true;
				}
				return false;
			}

			/**
			 * Returns the next element in the iteration.
			 * @return the next element in the iteration
			 * @throws NoSuchElementException
			 * if the iteration has no more elements
			 */
			@Override
			public Event next() throws NoSuchElementException{
				Event tmp = null;
				if (pos <= end){
					tmp = get(pos);
					++pos;
				}
				if (tmp == null){
					throw new NoSuchElementException();
				}
				return tmp;
			}
		};
	}

	/**
	 * Inserts the specified element into this queue if it is possible to do
	 * so immediately without violating capacity restrictions. When using
	 * a capacity-restricted queue, this method is generally preferable to
	 * {@link #add}, which can fail to insert an element only by
	 * throwing an exception.
	 *
	 * @param event the element to add
	 * @return true if the element was added to this queue, else false
	 * @throws NullPointerException if the specified element is null
	 */
	@Override
	public synchronized boolean offer(Event event)
			throws NullPointerException {
		if (event == null){
			//dont add null events
			throw new NullPointerException();
		}
		if (size >= maxArraySize && getTotalFreeElements() <= 0){
			//its completely full and cant resize larger
			return false;
		}

		//shift the array and resize if necessary
		if (shouldShift(1)){
			shiftToStart();
		}
		while (getFreeElementsAtEnd() < 1){
			//don't double size if its got a free space, but do double if full
			if (!doubleSize()){
				break;
			}
		}
		if (getTotalFreeElements() <= 0){
			//its completely full and cant resize larger
			return false;
		}
		if (getFreeElementsAtEnd() <= 0){
			return false;
		}

		//copy elements over
		try {
			array[tail] = event;
			//update tail
			++tail;
			return true;
		}
		catch (Exception e){
			e.printStackTrace(System.err);
			return false;//it failed copying
		}
	}

	/**
	 * Retrieves, but does not remove, the head of this queue, or returns
	 * null if this queue is empty.
	 *
	 * @return the head of this queue, or null if this queue is empty
	 */
	@Override
	public synchronized Event peek() {
		if (array.length <= head){
			return null;
		}
		return array[head];
	}

	/**
	 * Retrieves and removes the head of this queue, or returns null if
	 * this queue is empty.
	 * @return the head of this queue, or null if this queue is empty
	 */
	@Override
	public synchronized Event poll() {
		if (isEmpty()){
			return null;
		}
		Event toReturn = array[head];
		++head;
		return toReturn;
	}

	/**
	 * Retrieves and removes the head of this queue.
	 * This method differs from {@link #poll}
	 * only in that it throws an exception
	 * if this queue is empty.
	 *
	 * @return the head of this queue
	 * @throws NoSuchElementException if this queue is empty
	 */
	@Override
	public synchronized Event remove() throws NoSuchElementException{
		if (isEmpty()){
			throw new NoSuchElementException();
		}
		Event toReturn = array[head];
		++head;
		return toReturn;
	}

	/**
	 * Removes a single instance of the specified element from this
	 * collection, if it is present and still valid. More formally,
	 * removes an element e such that (o==null ? e==null : o.equals(e)),
	 * if this collection contains one or more such elements. Returns
	 * true if this collection contained the specified element (or
	 * equivalently, if this collection changed as a result of the call).
	 *
	 * @param object element to be removed from this collection, if present
	 * @return true if an element was removed as a result of this call
	 */
	@Override
	public synchronized boolean remove(Object object) {
		if (isEmpty()){
			return false;
		}
		if (!contains(object)){
			return false;
		}
		int firstIndex = 0;
		boolean found = false;

		for (firstIndex = head; firstIndex < tail; ++firstIndex){
			//if both are null or if the objects are not null and are equal
			if (object == null
					? array[firstIndex] == null
					: array[firstIndex].equals(object)){
				found = true;
				break;
			}
		}
		if (found){
			for (int i = firstIndex + 1; i < tail; ++i){
				/* shift everything after the object back one to cover it
				 * ignores everything in the array after the tail
				 */
				array[i-1] = array[i];
			}
			--tail;
			return true;
		}
		return false;
	}

	/**
	 * Removes all of this collection's elements that are also contained
	 * in the specified collection (optional operation). After this call
	 * returns, this collection will contain no elements in common
	 * with the specified collection.
	 *
	 * @param events collection containing elements to be removed from this
	 * collection
	 * @return true if this collection changed as a result of the call
	 */
	@Override
	public synchronized boolean removeAll(Collection<?> events) {
		boolean changed = false;
		for (Object object : events){
			changed = changed || remove(object);
			//if remove ever returns true, changed will be true
		}
		return changed;
	}

	/**
	 * Retains only the elements in this collection that are contained
	 * in the specified collection (optional operation). In other words,
	 * removes everything in the queue that is not in the supplied collection.
	 *
	 * @param events collection containing elements to be retained in
	 * this collection
	 * @return true if this collection changed as a result of the call
	 */
	@Override
	public synchronized boolean retainAll(Collection<?> events) {
		boolean changed = false;
		for (int i = head; i < tail; ++i){
			if (!events.contains(array[head])){
				changed = changed || remove(array[head]);
				--i;
			}
		}
		return changed;
	}

	/**
	 * Takes the head and tail and shifts the data to the
	 * beginning of the array.
	 *
	 * @return true on success, false for failure
	 */
	private synchronized boolean shiftToStart(){
		try{
			/* Copy from array starting at the old head
			 * position to itself beginning at the 0 index,
			 * total length equaling (tail-head)
			 */
			//faster than arraycopy
			for (int i = head; i < tail; ++i){
				array[head-i] = array[i];
				//head - i is the distance from head we are currently at
				//that is, position relative to 0 if head were at 0
			}
			//set head and tail to their new values
			tail -= head;
			head = 0;
			return true;
		}
		catch (Exception e){
			return false;
		}
	}

	/**
	 * Returns true if there is enough free space
	 * left to shift the array to the start instead
	 * of doubling size. Returns false if there is not
	 * enough space to add all elements. Only shifts if
	 * a certain percent of the array is free space.
	 * <p>
	 * This should be called before adding elements.
	 *
	 *@param elementsToAdd the number of elements to be added
	 * @return true if the array needs to be shifted, false otherwise
	 */
	private synchronized boolean shouldShift(int elementsToAdd){
		//not enough room
		if (getTotalFreeElements() < elementsToAdd){
			return false;
		}
		//Percentage free is greater than shiftPercentageUnused
		if ((size-(tail-head))/size >= shiftPercentageUnused){
			return true;
		}
		return false;
	}

	/**
	 * Returns the number of elements in this collection. If this collection
	 * contains more than Integer.MAX_VALUE elements, returns
	 * Integer.MAX_VALUE.
	 *
	 * @return the number of events left to dispatch
	 */
	@Override
	public synchronized int size() {
		if (size >= Integer.MAX_VALUE){
			//should never happen because size is an int.
			return Integer.MAX_VALUE;
		}
		return size;
	}

	/**
	 * Returns an array containing only valid
	 * events yet to be sent.
	 *
	 * @return the events that have yet to be dispatched
	 */
	@Override
	public synchronized Object[] toArray() {
		Object[] toReturn = new Object[tail-head];
		/*
		 * copy from array starting at head, to
		 * toReturn starting at 0, where the items to copy
		 * are of size tail-head (which exactly matches toReturn's size)
		 */
		System.arraycopy(array, head, toReturn, 0, tail - head);
		return toReturn;

	}

	/**
	 * Returns an array containing all of the valid events in the queue.
	 * the runtime type of the returned array is that of the specified array.
	 * If the collection fits in the specified array, it is returned therein.
	 * Otherwise, a new array is allocated with the runtime type of the
	 * specified array and the size of this collection.
	 *
	 * <p>If this collection fits in the specified array with room to spare
	 * (i.e., the array has more elements than this collection), the element
	 * in the array immediately following the end of the collection is set to
	 * <tt>null</tt>.  (This is useful in determining the length of this
	 * collection <i>only</i> if the caller knows that this collection does
	 * not contain any <tt>null</tt> elements.)
	 *
	 * <p>If this collection makes any guarantees as to what order its elements
	 * are returned by its iterator, this method must return the elements in
	 * the same order.
	 *
	 * <p>Like the {@link #toArray()} method, this method acts as bridge
	 * between
	 * array-based and collection-based APIs.  Further, this method allows
	 * precise control over the runtime type of the output array, and may,
	 * under certain circumstances, be used to save allocation costs.
	 *
	 * <p>Suppose <tt>x</tt> is a collection known to contain only strings.
	 * The following code can be used to dump the collection into a newly
	 * allocated array of <tt>String</tt>:
	 *
	 * <pre>
	 *     String[] y = x.toArray(new String[0]);</pre>
	 *
	 * Note that <tt>toArray(new Object[0])</tt> is identical in function to
	 * <tt>toArray()</tt>.
	 *
	 * @param <T> the runtime type of the array to contain the collection
	 * @param newArray the array into which the elements of this collection
	 * are to be
	 *        stored, if it is big enough; otherwise, a new array of the same
	 *        runtime type is allocated for this purpose.
	 * @return an array containing all of the elements in this collection
	 * @throws ArrayStoreException if the runtime type of the specified array
	 *         is not a supertype of the runtime type of every element in
	 *         this collection
	 * @throws NullPointerException if the specified array is null
	 */
	@SuppressWarnings("unchecked")
	@Override
	public synchronized <T> T[] toArray(T[] newArray)
			throws ArrayStoreException{
		//(head-tail) if (head-tail) > 0, otherwise its 0
		int size = tail-head > 0 ? tail-head : 0;
		if (newArray.length < size){
			newArray = (T[]) Array.newInstance(
					newArray.getClass().getComponentType(), size);
		}
		else if (newArray.length > size) {
			// If array is to large, set the first unassigned element to null
			newArray[size] = null;
		}
		//if the sizes are equal, its fits just fine

		int i = 0;
		for (Object e : toArray()) {
			// No need for checked cast - ArrayStoreException will be thrown
			// if types are incompatible, just as required
			newArray[i] = (T) e;
			++i;
		}

		return newArray;
	}

}
