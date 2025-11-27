class InvalidObjectIdError extends Error {
	constructor(message: string = "Invalid ObjectId", readonly fieldName: string = 'id') {
		super(message);
		this.fieldName = fieldName;
	}

	getFieldName() {
		return this.fieldName;
	}
}

class ValidationError extends Error {
	constructor(message: string = "Validation Error") {
		super(message);
	}
}

class ControllerError extends Error {
	readonly status: number;
	constructor(message: string = "Ocurrió un error", status: number = 500) {
		super(message);
		this.name = "ControllerError";
		this.status = status;
	}

	getStatus() {
		return this.status;
	}
}

class NotFoundError extends Error {
	constructor(message: string){
		super(message)
	}
}

class UnauthorizedError extends Error {
	constructor(message: string = "Unauthorized") {
		super(message);
		this.name = "UnauthorizedError";
	}
}

class ForbiddenError extends Error {
	constructor(message: string = "Forbidden") {
		super(message);
		this.name = "ForbiddenError";
	}
}
export {
    InvalidObjectIdError,
    ValidationError,
    ControllerError,
    NotFoundError,
    UnauthorizedError,
	ForbiddenError,
};