package tests;

import global.PageId;
import global.RID;
import global.SearchKey;
import index.HashIndex;
import index.HashScan;

/**
 * Test suite for the index layer.
 */
public class IXTest extends TestDriver {

  /** The display name of the test suite. */
  private static final String TEST_NAME = "hash index tests";

  /**
   * Size of index files to create in test cases.
   */
  private static final int FILE_SIZE = 3000;

  // --------------------------------------------------------------------------

  /**
   * Test application entry point; runs all tests.
   */
  public static void main(String argv[]) {

    // create a clean Minibase instance
    IXTest hft = new IXTest();
    hft.create_minibase();

    // run all the test cases
    System.out.println("\n" + "Running " + TEST_NAME + "...");
    boolean status = PASS;
    status &= hft.test1();
    status &= hft.test2();
    status &= hft.test3();

    // display the final results
    System.out.println();
    if (status != PASS) {
      System.out.println("Error(s) encountered during " + TEST_NAME + ".");
    } else {
      System.out.println("All " + TEST_NAME + " completed successfully!");
    }

  } // public static void main (String argv[])

  /**
   * Simple use of temp index.
   */
  protected boolean test1() {

    System.out.println();
    System.out.println("Test 1: Small, temporary hash index");
    initCounts();

    saveCounts(null);
    initRandom();

    System.out.println("\n  ~> creating temp index...");
    HashIndex temp = new HashIndex(null);
    for (int i = 0; i < 10; i++) {
      SearchKey key = randKey(1);
      RID rid = new RID(new PageId(i), 0);
      temp.insertEntry(key, rid);
    }

    saveCounts("create");
    saveCounts(null);
    initRandom();

    System.out.println("\n  ~> scanning temp index...");
    for (int i = 0; i < 10; i++) {

      // search for the random entry
      SearchKey key = randKey(1);
      RID rid = new RID(new PageId(i), 0);
      boolean found = false;
      HashScan scan = temp.openScan(key);
      while (scan.hasNext()) {
        RID rid2 = scan.getNext();
        if (rid2.equals(rid)) {
          found = true;
        }
      }
      // don't close, to test finalize

      if (!found) {
        System.out.println("  ERROR: Search key not found in scan!");
        return false;
      }

    } // for

    saveCounts("scan");
    saveCounts(null);

    System.out.println("\n  ~> deleting temp index...");
    temp = null;
    System.gc();

    saveCounts("delete");
    printSummary(3);

    System.out.println("\nTest 1 completed successfully.\n");
    return PASS;

  } // protected boolean test1()

  /**
   * HashIndex under normal conditions.
   */
  protected boolean test2() {

    System.out.println();
    System.out.println("Test 2: Larger, persistent hash indexes");
    initCounts();

    for (int type = 1; type <= 3; type++) {

      System.out.println("\n(type == " + type + ")");
      saveCounts(null);
      initRandom();

      System.out.print("\n  ~> building an index of " + FILE_SIZE + " ");
      if (type == 1) {
        System.out.print("integer");
      } else if (type == 2) {
        System.out.print("float");
      } else {
        System.out.print("string");
      }
      System.out.println("s...");

      String fileName = "IX_Customers" + type;
      HashIndex index = new HashIndex(fileName);
      for (int i = 0; i < FILE_SIZE; i++) {

        // insert a random entry
        SearchKey key = randKey(type);
        RID rid = new RID(new PageId(i), 0);
        index.insertEntry(key, rid);

      } // for

      saveCounts("ins" + type);
      index.printSummary();
      saveCounts(null);
      initRandom();

      System.out.println("\n  ~> scanning every other entry...");
      for (int i = 0; i < FILE_SIZE; i += 2) {

        // search for the random entry
        SearchKey key = randKey(type);
        randKey(type); // to keep in sync with inserts
        RID rid = new RID(new PageId(i), 0);
        boolean found = false;
        HashScan scan = index.openScan(key);
        while (scan.hasNext()) {
          RID rid2 = scan.getNext();
          if (rid2.equals(rid)) {
            found = true;
          }
        }
        scan.close();

        if (!found) {
          System.out.println("  ERROR: Search key not found in scan!");
          return false;
        }

      } // for

      saveCounts("scan" + type);
      saveCounts(null);
      initRandom();

      System.out.println("\n  ~> deleting every other entry...");
      for (int i = 0; i < FILE_SIZE; i += 2) {

        // delete the random entry
        SearchKey key = randKey(type);
        randKey(type); // to keep in sync with inserts
        RID rid = new RID(new PageId(i), 0);
        index.deleteEntry(key, rid);

      } // for

      saveCounts("del" + type);
      index.printSummary();
      saveCounts(null);

      // delete the file
      System.out.println("\n  ~> deleting the index file...");
      HashIndex byebye = new HashIndex(fileName);
      byebye.deleteFile();

      saveCounts("drop" + type);

    } // for type

    // made it without exceptions
    printSummary(4);
    System.out.println("\nTest 2 completed successfully.\n");
    return PASS;

  } // protected boolean test2()

  /**
   * Index package error cases.
   */
  protected boolean test3() {

    System.out.println();
    System.out.println("Test 3: Boundary and error conditions");
    initCounts();

    saveCounts(null);
    initRandom();

    System.out.println("\n  ~> creating temporary index...");
    HashIndex index = new HashIndex(null);

    saveCounts("create");
    saveCounts(null);

    System.out.println("\n  ~> inserting invalid entry...");
    SearchKey key = new SearchKey(new String(new byte[PAGE_SIZE]));
    RID rid = new RID();
    try {
      index.insertEntry(key, rid);
      System.out.println("     *** missing IllegalArgumentException");
      return FAIL;
    } catch (IllegalArgumentException exc) {
      System.out.println("     *** failed as expected");
    }

    System.out.println("\n  ~> deleting invalid entry...");
    key = randKey(1);
    try {
      index.deleteEntry(key, rid);
      System.out.println("     *** missing IllegalArgumentException");
      return FAIL;
    } catch (IllegalArgumentException exc) {
      System.out.println("     *** failed as expected");
    }

    System.out.println("\n  ~> next in completed scan...");
    HashScan scan = index.openScan(key);
    scan.hasNext();
    try {
      scan.getNext();
      System.out.println("     *** missing IllegalStateException");
      return FAIL;
    } catch (IllegalStateException exc) {
      System.out.println("     *** failed as expected");
    }

    saveCounts("errors");
    saveCounts(null);

    System.out.println("\n  ~> printing empty index...");
    index.printSummary();

    saveCounts("print");
    saveCounts(null);

    System.out.println("\n  ~> deleting empty index...");
    index = null;
    System.gc();

    saveCounts("delete");
    printSummary(4);

    System.out.println("\nTest 3 completed successfully.\n");
    return PASS;

  } // protected boolean test3()

  // --------------------------------------------------------------------------

  /**
   * Gets a random search key, given the type.
   */
  protected SearchKey randKey(int type) {
    switch (type) {
      case 1:
        return new SearchKey(random.nextInt());
      case 2:
        return new SearchKey(random.nextFloat());
      case 3:
        return new SearchKey(randEmail());
      default:
        throw new IllegalArgumentException("unknown type");
    }
  } // protected SearchKey randKey(int type)

  /**
   * Test random email generator.
   */
  protected void testrand() {

    int size_sum = 0;
    int size_min = Integer.MAX_VALUE;
    int size_max = Integer.MIN_VALUE;

    for (int i = 0; i < FILE_SIZE; i++) {
      String email = randEmail();
      System.out.println(email);
      int size = email.length();
      if (size < size_min)
        size_min = size;
      if (size > size_max)
        size_max = size;
      size_sum += email.length();
    }

    System.out.println("Minimum size = " + size_min);
    System.out.println("Maximum size = " + size_max);
    System.out.println("Average size = " + (size_sum / FILE_SIZE));

  } // protected void testrand()

  /**
   * Using the novel algorithm described here:
   * http://www.homestarrunner.com/sbemail143.html
   */
  protected String randEmail() {

    // hobby (random letters)
    String email = "";
    int size = Math.abs(random.nextInt() % 5) + 4;
    for (int i = 0; i < size; i++) {
      email += (char) (Math.abs(random.nextInt() % 26) + 97);
    }

    // middle part
    switch (Math.abs(random.nextInt() % 4)) {
      case 0:
        email += "kid";
        break;
      case 1:
        email += "grrl";
        break;
      case 2:
        email += "pie";
        break;
      default:
        email += "izzle";
        break;
    }

    // some numbers
    size = Math.abs(random.nextInt() % 4) + 2;
    for (int i = 0; i < size; i++) {
      email += Math.abs(random.nextInt() % 10);
    }

    // suffix, not "@kindergartencop.edu" for variety ;)
    email += "@";
    size = Math.abs(random.nextInt() % 16) + 4;
    for (int i = 0; i < size; i++) {
      email += (char) (Math.abs(random.nextInt() % 26) + 97);
    }
    return email + ".edu";

  } // protected String randEmail()

} // class IXTest extends TestDriver
