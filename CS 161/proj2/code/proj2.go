package proj2


// You MUST NOT change what you import.  If you add ANY additional
// imports it will break the autograder, and we will be Very Upset.

import (
	// You neet to add with
	// go get github.com/nweaver/cs161-p2/userlib
	"github.com/nweaver/cs161-p2/userlib"

	// Life is much easier with json:  You are
	// going to want to use this so you can easily
	// turn complex structures into strings etc...
	"encoding/json"

	// Likewise useful for debugging etc
	"encoding/hex"
	
	// UUIDs are generated right based on the crypto RNG
	// so lets make life easier and use those too...
	//
	// You need to add with "go get github.com/google/uuid"
	"github.com/google/uuid"

	// For the useful little debug printing function
	"fmt"
	"time"
	"os"
	"strings"

	// I/O
	"io"
	
	// Want to import errors
	"errors"
	
	// These are imported for the structure definitions.  You MUST
	// not actually call the functions however!!!
	// You should ONLY call the cryptographic functions in the
	// userlib, as for testing we may add monitoring functions.
	// IF you call functions in here directly, YOU WILL LOSE POINTS
	// EVEN IF YOUR CODE IS CORRECT!!!!!
	"crypto/rsa"
)


// This serves two purposes: It shows you some useful primitives and
// it suppresses warnings for items not being imported
func someUsefulThings(){
	// Creates a random UUID
	f := uuid.New()
	debugMsg("UUID as string:%v", f.String())
	
	// Example of writing over a byte of f
	f[0] = 10
	debugMsg("UUID as string:%v", f.String())

	// takes a sequence of bytes and renders as hex
	h := hex.EncodeToString([]byte("fubar"))
	debugMsg("The hex: %v", h)
	
	// Marshals data into a JSON representation
	// Will actually work with go structures as well
	d,_ := json.Marshal(f)
	debugMsg("The json data: %v", string(d))
	var g uuid.UUID
	json.Unmarshal(d, &g)
	debugMsg("Unmashaled data %v", g.String())

	// This creates an error type
	debugMsg("Creation of error %v", errors.New("This is an error"))

	// And a random RSA key.  In this case, ignoring the error
	// return value
	var key *rsa.PrivateKey
	key,_ = userlib.GenerateRSAKey()
	debugMsg("Key is %v", key)
}


// Helper function: Takes the first 16 bytes and
// converts it into the UUID type
func bytesToUUID(data []byte) (ret uuid.UUID) {
	for x := range(ret){
		ret[x] = data[x]
	}
	return
}


// Helper function: Returns a byte slice of the specificed
// size filled with random data
func randomBytes(bytes int) (data []byte){
	data = make([]byte, bytes)
	if _, err := io.ReadFull(userlib.Reader, data); err != nil {
		panic(err)
	}
	return
}


var DebugPrint = false


// Helper function: Does formatted printing to stderr if
// the DebugPrint global is set.  All our testing ignores stderr,
// so feel free to use this for any sort of testing you want
func debugMsg(format string, args ...interface{}) {
	if DebugPrint{
		msg := fmt.Sprintf("%v ", time.Now().Format("15:04:05.00000"))
		fmt.Fprintf(os.Stderr,
			msg + strings.Trim(format, "\r\n ") + "\n", args...)
	}
}


// The structure definition for a user record
// FileNames maps what you call the file to what the original creator calls the file
// FileKeys maps what you call the file to the file's symmetric key
type User struct {
	Username string
	Password string
	PrivateKey *rsa.PrivateKey
	FileNames map[string] string
	FileKeys map[string] []byte
	// You can add other fields here if you want...
	// Note for JSON to marshal/unmarshal, the fields need to
	// be public (start with a capital letter)
}


// The structure defined for files
// Creators is the user name of the user that created the corresponding (by index) data or append
// Data holds the data for a file and any appends that it may have
// Sign holds RSA signitures on the corresponding (by index) data or append
// Shared holds the user names of users the file is shared with
type File struct {
	Creators []string
	Data [][]byte
	Sign [][]byte
	Shared map[string] int
}


// Calculates the mac for the given data and given key
func MacCalculate(data []byte, key []byte) (calculated_mac []byte){
	hash := userlib.NewSHA256()
	hash.Write(key)
	key_hash := hash.Sum(nil)

	mac := userlib.NewHMAC(key_hash)
	mac.Write(data)
	return mac.Sum(nil)
}


// Encrypts the given data using the given key
// Then macs the encrypted data using the given key	
func EncryptMacData(data []byte, key []byte) (encrypted []byte){
	hash := userlib.NewSHA256()
	hash.Write(key)
	key_hash := hash.Sum(nil)

	data_encrypt := make([]byte, len(data))
	iv := randomBytes(userlib.BlockSize)
	cipher := userlib.CFBEncrypter(key_hash, iv)
	cipher.XORKeyStream(data_encrypt, data)
	data_encrypt = append(iv, data_encrypt...)
	mac := MacCalculate(data_encrypt, key)
	data_encrypt = append(mac, data_encrypt...)

	return data_encrypt
}


// Decrypts the given data with the given key
func DecryptData(data []byte, key []byte) (decrypted []byte){
	hash := userlib.NewSHA256()
	hash.Write(key)
	key_hash := hash.Sum(nil)

	iv := data[userlib.HashSize:userlib.HashSize + userlib.BlockSize]
	cipher := userlib.CFBDecrypter(key_hash, iv)
	data_decrypt := make([]byte, len(data) - userlib.HashSize - userlib.BlockSize)
	cipher.XORKeyStream(data_decrypt, data[userlib.HashSize + userlib.BlockSize:])

	return data_decrypt
}


// This creates a user.  It will only be called once for a user
// (unless the keystore and datastore are cleared during testing purposes)

// It should store a copy of the userdata, suitably encrypted, in the
// datastore and should store the user's public key in the keystore.

// The datastore may corrupt or completely erase the stored
// information, but nobody outside should be able to get at the stored
// User data: the name used in the datastore should not be guessable
// without also knowing the password and username.

// You are not allowed to use any global storage other than the
// keystore and the datastore functions in the userlib library.

// You can assume the user has a STRONG password
func InitUser(username string, password string) (userdataptr *User, err error){
	key, err1 := userlib.GenerateRSAKey()

	if err1 != nil{
		return nil, err1
	}

	// Create userdata and populate fields
	var userdata User
	userdata.Username = username
	userdata.Password = password
	userdata.PrivateKey = key
	userdata.FileNames = make(map[string] string)
	userdata.FileKeys = make(map[string] []byte)

	// Encrypt userdata
	userdata_json, _ := json.Marshal(userdata)
	userdata_encrypt := EncryptMacData(userdata_json, []byte(password))

	// Make userdata stored location unique based off of username and password
	userdata_location := userlib.PBKDF2Key([]byte(username), []byte(password), 64)
	userlib.DatastoreSet(string(userdata_location), userdata_encrypt)

	// Store public key under username
	userlib.KeystoreSet(username, key.PublicKey)

	return &userdata, nil
}


// This fetches the user information from the Datastore.  It should
// fail with an error if the user/password is invalid, or if the user
// data was corrupted, or if the user can't be found.
func GetUser(username string, password string) (userdataptr *User, err error){
	// Create userdata location based off of username and password
	userdata_location := userlib.PBKDF2Key([]byte(username), []byte(password), 64)
	userdata_encrypt, ok1 := userlib.DatastoreGet(string(userdata_location))

	// If the get fails, then the username and/or password is incorrect
	if !ok1{
		return nil, errors.New("User/password incorrect.")
	}

	// Check for corruption of userdata
	ok2 := userlib.Equal(MacCalculate(userdata_encrypt[userlib.HashSize:], []byte(password)), userdata_encrypt[:userlib.HashSize])
	if !ok2{
		return nil, errors.New("User data corrupted.")
	}

	// Decrypt userdata
	userdata_decrypt := DecryptData(userdata_encrypt, []byte(password))

	// Populate userdata fields
	var userdata User
	json.Unmarshal(userdata_decrypt, &userdata)

	return &userdata, nil
}


// This stores a file in the datastore.
//
// The name of the file should NOT be revealed to the datastore!
func (userdata *User) StoreFile(filename string, data []byte) {
	// Make file stored location unique based off of private key and filename
	key_json, _ := json.Marshal(userdata.PrivateKey)
	mac := userlib.NewHMAC(key_json)
	mac.Write([]byte(filename))
	mac_value := mac.Sum(nil)
	file_location := string(mac_value)
	userdata.FileNames[file_location] = file_location

	// Generate random symmetric key
	key := randomBytes(userlib.RSAKeySize/8)
	hash := userlib.NewSHA256()
	hash.Write(key)
	userdata.FileKeys[file_location] = hash.Sum(nil)

	// Update mac for userdata
	userdata_json, _ := json.Marshal(userdata)
	userdata_encrypt := EncryptMacData(userdata_json, []byte(userdata.Password))
	userdata_location := userlib.PBKDF2Key([]byte(userdata.Username), []byte(userdata.Password), 64)
	userlib.DatastoreSet(string(userdata_location), userdata_encrypt)

	// Encrypt the file data to store
	data_encrypt := EncryptMacData(data, hash.Sum(nil))

	// Create filedata and populate fields
	var filedata File
	filedata.Creators = append(filedata.Creators, userdata.Username)
	filedata.Data = append(filedata.Data, data_encrypt)
	sign, _ := userlib.RSASign(userdata.PrivateKey, data_encrypt)
	filedata.Sign = append(filedata.Sign, sign)
	filedata.Shared = make(map[string] int)
	filedata.Shared[userdata.Username] = 1

	filedata_json, _ := json.Marshal(filedata)

	userlib.DatastoreSet(file_location, filedata_json)
}


// This adds on to an existing file.
//
// Append should be efficient, you shouldn't rewrite or reencrypt the
// existing file, but only whatever additional information and
// metadata you need.

func (userdata *User) AppendFile(filename string, data []byte) (err error){
	// Create file location based off of private key and filename
	key_json, _ := json.Marshal(userdata.PrivateKey)
	mac := userlib.NewHMAC(key_json)
	mac.Write([]byte(filename))
	mac_value := mac.Sum(nil)
	file_location := string(mac_value)
	file_location_real, ok1 := userdata.FileNames[file_location]

	// If the get fails, then the filename is incorrect
	if !ok1{
		return errors.New("File not found.")
	}

	data_json, ok2 := userlib.DatastoreGet(file_location_real)

	// If the get fails, then the filename is incorrect
	if !ok2{
		return errors.New("File not found.")
	}

	// Create filedata and populate fields
	var filedata File
	json.Unmarshal(data_json, &filedata)

	// Check for share permission
	shared, ok3 := filedata.Shared[userdata.Username]
	if !ok3 || shared != 1{
		return errors.New("Permission denied.")
	}

	// Encrypt the file data to append
	data_encrypt := EncryptMacData(data, userdata.FileKeys[file_location])

	// Add to filedata fields
	filedata.Creators = append(filedata.Creators, userdata.Username)
	filedata.Data = append(filedata.Data, data_encrypt)
	sign, _ := userlib.RSASign(userdata.PrivateKey, data_encrypt)
	filedata.Sign = append(filedata.Sign, sign)

	filedata_json, _ := json.Marshal(filedata)
	userlib.DatastoreSet(file_location_real, filedata_json)

	return nil
}


// This loads a file from the Datastore.
//
// It should give an error if the file is corrupted in any way.
func (userdata *User) LoadFile(filename string)(data []byte, err error) {
	// Create file location based off of private key and filename
	key_json, _ := json.Marshal(userdata.PrivateKey)
	mac := userlib.NewHMAC(key_json)
	mac.Write([]byte(filename))
	mac_value := mac.Sum(nil)
	file_location := string(mac_value)
	file_location_real, ok1 := userdata.FileNames[file_location]

	// If the get fails, then the filename is incorrect
	if !ok1{
		return nil, errors.New("File not found.")
	}

	data_json, ok2 := userlib.DatastoreGet(file_location_real)

	// If the get fails, then the filename is incorrect
	if !ok2{
		return nil, errors.New("File not found.")
	}

	// Create filedata and populate fields
	var filedata File
	json.Unmarshal(data_json, &filedata)

	// Check for share permission
	shared, ok3 := filedata.Shared[userdata.Username]
	if !ok3 || shared != 1{
		return nil, errors.New("Permission denied.")
	}

	// Check for corruption
	for i := 0; i < len(filedata.Creators); i++{
		if !userlib.Equal(MacCalculate(filedata.Data[i][userlib.HashSize:], userdata.FileKeys[file_location]), filedata.Data[i][:userlib.HashSize]){
			return nil, errors.New("File data corrupted (or permission denied).")
		}

		public_key, ok4 := userlib.KeystoreGet(filedata.Creators[i])
		if !ok4{
			return nil, errors.New("File data corrupted (or permission denied).")
		}
		err1 := userlib.RSAVerify(&public_key, filedata.Data[i], filedata.Sign[i])
		if err1 != nil{
			return nil, errors.New("File data corrupted (or permission denied).")
		}
	}

	// If file is in multiple parts, append them together
	data_return := make([]byte, 0)
	for j := 0; j < len(filedata.Creators); j++{
		data_decrypt := DecryptData(filedata.Data[j], userdata.FileKeys[file_location])
		data_return = append(data_return, data_decrypt...)
	}

	return data_return, nil
}


// You may want to define what you actually want to pass as a
// sharingRecord to serialized/deserialize in the data store.
// FileLocation is where the file is stored
// Symmetric key is the key used to encrypt and mac the data files
type sharingRecord struct {
	FileLocation []byte
	SymmetricKey []byte
}


// This creates a sharing record, which is a key pointing to something
// in the datastore to share with the recipient.

// This enables the recipient to access the encrypted file as well
// for reading/appending.

// Note that neither the recipient NOR the datastore should gain any
// information about what the sender calls the file.  Only the
// recipient can access the sharing record, and only the recipient
// should be able to know the sender.
func (userdata *User) ShareFile(filename string, recipient string)(
	msgid string, err error){
	public_key, ok1 := userlib.KeystoreGet(recipient)
	if !ok1{
		return "", errors.New("Recipient not found.")
	}

	// Create file location based off of private key and filename
	key_json1, _ := json.Marshal(userdata.PrivateKey)
	mac1 := userlib.NewHMAC(key_json1)
	mac1.Write([]byte(filename))
	mac_value1 := mac1.Sum(nil)
	file_location := string(mac_value1)
	file_location_real := userdata.FileNames[file_location]

	filedata_json, ok2 := userlib.DatastoreGet(file_location_real)
	if !ok2{
		return "", errors.New("File not found.")
	}

	var filedata File
	json.Unmarshal(filedata_json, &filedata)

	// Check for share permission
	shared, ok2 := filedata.Shared[userdata.Username]
	if !ok2 || shared != 1{
		return "", errors.New("Permission denied.")
	}

	// Add recipient to list of shared users
	filedata.Shared[recipient] = 1
	filedata_update_json, _ := json.Marshal(filedata)
	userlib.DatastoreSet(file_location_real, filedata_update_json)

	// Create share data and populate fields
	var share_data sharingRecord
	share_data.FileLocation = []byte(file_location_real)

	key_encrypt, _ := userlib.RSAEncrypt(&public_key, userdata.FileKeys[file_location], []byte("Tag"))
	share_data.SymmetricKey = key_encrypt

	share_data_json, _ := json.Marshal(share_data)

	key_json2, _ := json.Marshal(userdata.FileKeys[file_location])
	mac2 := userlib.NewHMAC(key_json2)
	mac2.Write([]byte(filename))
	mac_value2 := mac2.Sum(nil)
	share_data_location := string(mac_value2)

	userlib.DatastoreSet(share_data_location, share_data_json)

	share_data_encrypt, _ := userlib.RSAEncrypt(&public_key, []byte(share_data_location), []byte("Tag"))

	sign, _ := userlib.RSASign(userdata.PrivateKey, share_data_encrypt)
	ret := append(sign, share_data_encrypt...)

	return string(ret), nil
}


// Note recipient's filename can be different from the sender's filename.
// The recipient should not be able to discover the sender's view on
// what the filename even is!  However, the recipient must ensure that
// it is authentically from the sender.
func (userdata *User) ReceiveFile(filename string, sender string,
	msgid string) error {
	public_key, ok := userlib.KeystoreGet(sender)
	if !ok{
		return errors.New("Sender not found.")
	}

	data := []byte(msgid)

	err := userlib.RSAVerify(&public_key, data[256:], data[:256])
	if err != nil{
		return errors.New("Share data corrupted (or invalid).")
	}

	data_decrypt, _ := userlib.RSADecrypt(userdata.PrivateKey, data[256:], []byte("Tag"))

	share_data_json, ok2 := userlib.DatastoreGet(string(data_decrypt))
	if !ok2{
		return errors.New("Share data corrupted or invalid.")
	}

	var share_data sharingRecord
	json.Unmarshal(share_data_json, &share_data)

	key_json, _ := json.Marshal(userdata.PrivateKey)
	mac := userlib.NewHMAC(key_json)
	mac.Write([]byte(filename))
	mac_value := mac.Sum(nil)
	file_location := string(mac_value)
	userdata.FileNames[file_location] = string(share_data.FileLocation)

	key_decrypt, _ := userlib.RSADecrypt(userdata.PrivateKey, share_data.SymmetricKey, []byte("Tag"))
	userdata.FileKeys[file_location] = key_decrypt

	// Update mac for userdata
	userdata_json, _ := json.Marshal(userdata)
	userdata_encrypt := EncryptMacData(userdata_json, []byte(userdata.Password))
	userdata_location := userlib.PBKDF2Key([]byte(userdata.Username), []byte(userdata.Password), 64)
	userlib.DatastoreSet(string(userdata_location), userdata_encrypt)

	return nil
}


// Removes access for all others.  
func (userdata *User) RevokeFile(filename string) (err error){
	// Create file location based off of private key and filename
	key_json, _ := json.Marshal(userdata.PrivateKey)
	mac := userlib.NewHMAC(key_json)
	mac.Write([]byte(filename))
	mac_value := mac.Sum(nil)
	file_location := string(mac_value)
	data_json, ok := userlib.DatastoreGet(file_location)

	if !ok{
		return errors.New("File not found (or permission denied).")
	}

	var filedata File
	json.Unmarshal(data_json, &filedata)

	// Generate new symmetric key
	key := randomBytes(userlib.RSAKeySize/8)

	// Encrypt, mac, and sign all data with new key
	for i := 0; i < len(filedata.Creators); i++{
		// Change file or append creator to self
		filedata.Creators[i] = userdata.Username

		// First unencrypt the data using the previous key
		data_decrypt := DecryptData(filedata.Data[i], userdata.FileKeys[file_location])

		// Then re-encrypt data with new key
		data_re_encrypt := EncryptMacData(data_decrypt, key)
		filedata.Data[i] = data_re_encrypt

		// Sign data with own private key
		filedata.Sign[i], _ = userlib.RSASign(userdata.PrivateKey, data_re_encrypt)
	}

	// Update symmetric key for self
	userdata.FileKeys[file_location] = key

	// Erase all share permissions other than for self
	filedata.Shared = make(map[string] int)
	filedata.Shared[userdata.Username] = 1

	filedata_json, _ := json.Marshal(filedata)
	userlib.DatastoreSet(file_location, filedata_json)

	return nil
}
