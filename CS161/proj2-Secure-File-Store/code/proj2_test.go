package proj2


import "testing"
// You can actually import other stuff if you want IN YOUR TEST
// HARNESS ONLY.  Note that this is NOT considered part of your
// solution, but is how you make sure your solution is correct.


// func TestInit(t *testing.T){
// 	t.Log("Initialization test")
// 	DebugPrint = true
// 	someUsefulThings()

// 	DebugPrint = false
// 	u, err := InitUser("alice","fubar")
// 	if err != nil {
// 		// t.Error says the test fails 
// 		t.Error("Failed to initialize user", err)
// 	}
// 	// t.Log() only produces output if you run with "go test -v"
// 	t.Log("Got user", u)
// 	// You probably want many more tests here.
// }


func TestFunction(t *testing.T){
	// And some more tests, because
	_, err1 := GetUser("alice", "fubar")
	if err1 == nil{
		t.Error("Returned an invalid user.")
		return
	}

	t.Log("Creating Alice.")
	InitUser("alice","fubar")
	u, err2 := GetUser("alice", "fubar")
	if err2 != nil{
		t.Error("Failed to return a valid user.")
		return
	}
	t.Log("Creating file for Alice.")
	u.StoreFile("hello", []byte("a"))

	t.Log("Creating Bob.")
	InitUser("bob","foo")
	v, err3 := GetUser("bob", "foo")
	if err3 != nil{
		t.Error("Failed to return a valid user.")
		return
	}
	t.Log("Creating file for Bob.")
	v.StoreFile("hello", []byte("b"))

	t.Log("Creating Eve.")
	InitUser("eve","ooo")
	w, err4 := GetUser("eve", "ooo")
	if err4 != nil{
		t.Error("Failed to return a valid user.")
		return
	}
	t.Log("Creating file for Eve.")
	w.StoreFile("bye", []byte("c"))

	t.Log("Loading Alice's file.")
	_, err5 := u.LoadFile("hello")
	if err5 != nil{
		t.Error("Failed to load valid file.")
		return
	}

	t.Log("Loading Bob's file.")
	_, err6 := v.LoadFile("hello")
	if err6 != nil{
		t.Error("Failed to load valid file.")
		return
	}

	t.Log("Loading Eve's file.")
	_, err7 := w.LoadFile("bye")
	if err7 != nil{
		t.Error("Failed to load valid file.")
		return
	}

	t.Log("Attempting to return an invalid file for Alice.")
	_, err8 := u.LoadFile("bye")
	if err8 == nil{
		t.Error("Returned an invalid file.")
		return
	}

	t.Log("Appending to Alice's file.")
	err10 := u.AppendFile("hello", []byte("a"))
	if err10 != nil{
		t.Error("Failed to append to a valid file.")
		return
	}

	t.Log("Attempting to append to an invalid file for Eve.")
	err11 := w.AppendFile("hello", []byte("a"))
	if err11 == nil{
		t.Error("Appended to an invalid file.")
		return
	}

	t.Log("Sharing Alice's file with Bob.")
	mmsgid1, err12 := u.ShareFile("hello", "bob")
	if err12 != nil{
		t.Error("Failed to share a valid file.")
		return
	}

	t.Log("Attempting to share an invalid file for Alice.")
	_, err13 := u.ShareFile("bye", "bob")
	if err13 == nil{
		t.Error("Shared an invalid file.")
		return
	}

	t.Log("Bob recieving Alice's share data.")
	err14 := v.ReceiveFile("bye", "alice", mmsgid1)
	if err14 != nil{
		t.Error("Failed to recieve a valid file.")
		return
	}

	t.Log("Bob now appending to Alice's shared file.")
	err15 := v.AppendFile("bye", []byte("a"))
	if err15 != nil{
		t.Error("Failed to append to a valid file.")
		return
	}

	t.Log("Bob attempting to revoking access to Alice's file.")
	err16 := v.RevokeFile("bye")
	if err16 == nil{
		t.Error("Performed an invalid revoke.")
		return
	}

	t.Log("Alice revoking access to her file.")
	err17 := u.RevokeFile("hello")
	if err17 != nil{
		t.Error("Failed to perform a valid revoke.")
		return
	}

	t.Log("Bob attempting to append to Alice's revoked file.")
	err18 := v.AppendFile("bye", []byte("a"))
	if err18 == nil{
		t.Error("Appended to an invalid file.")
		return
	}

	t.Log("Alice appending to her file.")
	err19 := u.AppendFile("hello", []byte("a"))
	if err19 != nil{
		t.Error("Failed to append to a valid file.")
		return
	}

	t.Log("Success")
}


func TestDataShare(t *testing.T){
	t.Log("Creating Alice.")
	InitUser("alice","fubar")
	u, _ := GetUser("alice", "fubar")

	t.Log("Creating Bob.")
	InitUser("bob","foo")
	v, _ := GetUser("bob", "foo")

	t.Log("Creating Eve.")
	InitUser("eve","ooo")
	w, _ := GetUser("eve", "ooo")

	t.Log("Creating file for Alice.")
	u.StoreFile("hello", []byte("a"))

	t.Log("Sharing Alice's file with Bob.")
	mmsgid1, _ := u.ShareFile("hello", "bob")

	t.Log("Bob recieving Alice's share data.")
	v.ReceiveFile("bye", "alice", mmsgid1)

	t.Log("Bob sharing Alice's file with Eve.")
	mmsgid2, _ := v.ShareFile("bye", "eve")

	t.Log("Eve recieving Alice's share data.")
	w.ReceiveFile("hello", "bob", mmsgid2)

	f1, err11 := u.LoadFile("hello")
	g1, err12 := v.LoadFile("bye")
	h1, err13 := w.LoadFile("hello")
	if err11 != nil || err12 != nil || err13 != nil{
		t.Error("Bad")
		return
	}
	if f1[0] != g1[0] || f1[0] != h1[0]{
		t.Error("Bad")
		return
	}

	t.Log("Alice appending to her file.")
	u.AppendFile("hello", []byte("b"))

	f2, err21 := u.LoadFile("hello")
	g2, err22 := v.LoadFile("bye")
	h2, err23 := w.LoadFile("hello")
	if err21 != nil || err22 != nil || err23 != nil{
		t.Error("Bad")
		return
	}
	if f2[0] != g2[0] || f2[0] != h2[0]{
		t.Error("Bad")
		return
	}
	if f2[1] != g2[1] || f2[1] != h2[1]{
		t.Error("Bad")
		return
	}

	t.Log("Bob appending to Alice's shared file.")
	v.AppendFile("bye", []byte("c"))

	f3, err31 := u.LoadFile("hello")
	g3, err32 := v.LoadFile("bye")
	h3, err33 := w.LoadFile("hello")
	if err31 != nil || err32 != nil || err33 != nil{
		t.Error("Bad")
		return
	}
	if f3[0] != g3[0] || f3[0] != h3[0]{
		t.Error("Bad")
		return
	}
	if f3[1] != g3[1] || f3[1] != h3[1]{
		t.Error("Bad")
		return
	}
	if f3[2] != g3[2] || f3[2] != h3[2]{
		t.Error("Bad")
		return
	}

	t.Log("Eve appending to Alice's shared file.")
	w.AppendFile("hello", []byte("d"))

	f4, err41 := u.LoadFile("hello")
	g4, err42 := v.LoadFile("bye")
	h4, err43 := w.LoadFile("hello")
	if err41 != nil || err42 != nil || err43 != nil{
		t.Error("Bad")
		return
	}
	if f4[0] != g4[0] || f4[0] != h4[0] {
		t.Error("Bad")
		return
	}
	if f4[1] != g4[1] || f4[1] != h4[1] {
		t.Error("Bad")
		return
	}
	if f4[2] != g4[2] || f4[2] != h4[2] {
		t.Error("Bad")
		return
	}
	if f4[3] != g4[3] || f4[3] != h4[3] {
		t.Error("Bad")
		return
	}

	t.Log("Alice revoking access to her file.")
	u.RevokeFile("hello")

	t.Log("Bob attempting to append to Alice's revoked file.")
	v.AppendFile("bye", []byte("c"))
	t.Log("Eve attempting to append to Alice's revoked file.")
	w.AppendFile("hello", []byte("d"))

	f5, err1 := u.LoadFile("hello")
	if (len(f5) != 4){
		t.Error("Bob and Eve appended to file after revoked.")
		return
	}
	if err1 != nil{
		t.Error("Alice lost access to her file.")
		return
	}

	t.Log("Bob attempting to load Alice's revoked file.")
	_, err2 := v.LoadFile("bye")
	if err2 == nil{
		t.Error("Bob accessed file after revoked.")
		return
	}

	t.Log("Eve attempting to load Alice's revoked file.")
	_, err3 := w.LoadFile("hello")
	if err3 == nil{
		t.Error("Eve accessed file after revoked.")
		return
	}

	t.Log("Success")
}
