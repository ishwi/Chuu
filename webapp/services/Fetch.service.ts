
export const fetchPost = <T, Y>(url: string, body: T): Promise<Y> => {
    let token = localStorage.getItem('token');
    let concat = `http://localhost:8080${url}`;
    console.log(concat)
    console.log(token)
    console.log("im here")


    return new Promise((resolve, reject) => {
        fetch(concat, {
            headers: {
                Authorization: 'Bearer ' + token
            },
            method: 'POST',
            body: JSON.stringify(body)
        })
            .then(response => {
                console.log(response)
                if (!response.ok) {
                    throw new Error(response.statusText)
                }
                return response.json() as Promise<Y>;
            })
            .then(resolve, e => reject('Failed!'));
    });
}

const fetchGet = <T>(url: string): Promise<T> => {
    let token = localStorage.getItem('token');
    let concat = `http://localhost:8080${url}`;
    console.log(concat)
    console.log(token)
    console.log("im here")


    return new Promise((resolve, reject) => {
        fetch(concat, {
            headers: {
                Authorization: 'Bearer ' + token
            },
            method: "GET"
        })
            .then(response => {
                console.log(response)
                if (!response.ok) {
                    throw new Error(response.statusText)
                }
                return response.json() as Promise<T>;
            })
            .then(resolve, e => reject('Failed!'))
    });
}

export default fetchGet;
